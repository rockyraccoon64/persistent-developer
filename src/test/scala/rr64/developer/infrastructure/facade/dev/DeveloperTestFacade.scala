package rr64.developer.infrastructure.facade.dev

import akka.actor.typed.ActorSystem
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import akka.persistence.typed.PersistenceId
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers._
import rr64.developer.domain.task.Difficulty
import rr64.developer.domain.timing.{Factor, Timing}
import rr64.developer.infrastructure.dev.behavior.{Command, DeveloperBehavior, State}
import rr64.developer.infrastructure.facade.event.DeveloperEventTestFacade._
import rr64.developer.infrastructure.facade.task.{TestTask, TestTaskIdentifier, TestTaskWithId}

import scala.concurrent.duration.FiniteDuration

class DeveloperTestFacade(workFactor: Int, restFactor: Int)
    (implicit system: ActorSystem[_]) {

  private type Kit = EventSourcedBehaviorTestKit[Command, Event, State]

  private val _workFactor = Factor(workFactor)
  private val _restFactor = Factor(restFactor)
  private val developerTestKit: Kit =
    EventSourcedBehaviorTestKit(
      system = system,
      behavior = DeveloperBehavior(
        persistenceId = PersistenceId.ofUniqueId("dev-test"),
        workFactor = _workFactor,
        restFactor = _restFactor
      ),
      SerializationSettings.disabled
    )

  def reset(): Unit = developerTestKit.clear()

  def fail(): Unit = developerTestKit.restart()

  def shouldBeFree: Assertion =
    developerTestKit.getState() shouldEqual State.Free

  def shouldBeWorkingOnTask(task: TestTask): Assertion =
    inside(developerTestKit.getState()) {
      case working: State.Working =>
        working.currentTask.task shouldEqual task.toDomain
    }

  def shouldNotBeWorking: Assertion =
    developerTestKit.getState() should not be a [State.Working]

  def shouldBeRestingAfterCompletingTask(task: TestTask): Assertion =
    inside(developerTestKit.getState()) {
      case resting: State.Resting =>
        resting.lastCompleted.task shouldEqual task.toDomain
    }

  def shouldBeResting: Assertion =
    developerTestKit.getState() shouldBe a [State.Resting]

  def shouldNotBeResting: Assertion =
    developerTestKit.getState() should not be a [State.Resting]

  def afterStartingTask(task: TestTaskWithId): Unit =
    developerTestKit.initialize(taskStartedEvent(task.toDomain))

  def afterCompletingTask(task: TestTaskWithId): Unit = {
    val domainTask = task.toDomain
    developerTestKit.initialize(
      taskStartedEvent(domainTask),
      taskFinishedEvent(domainTask)
    )
  }

  def whileResting(lastCompleted: TestTaskWithId, taskQueue: Seq[TestTaskWithId]): Unit =
    developerTestKit.initialize(State.Resting(lastCompleted.toDomain, taskQueue.map(_.toDomain)))

  def addTask(task: TestTask): AddTaskResultTestFacade = {
    val result = developerTestKit.runCommand(Command.AddTask(task.toDomain, _))
    new AddTaskResultTestFacade(result)
  }

  def workingOnTaskWithReturnedIdentifier(id: TestTaskIdentifier): Assertion = {
    inside(developerTestKit.getState()) {
      case working: State.Working =>
        working.currentTask.id shouldEqual id.id
    }
  }

  def queueShouldEqual(tasks: Seq[TestTask]): Assertion = {
    val queue = developerTestKit.getState() match {
      case working: State.Working => working.taskQueue
      case resting: State.Resting => resting.taskQueue
    }
    queue.map(_.task) should contain theSameElementsInOrderAs tasks.map(_.toDomain)
  }

  /** Расчитать время работы */
  def calculateWorkTime(task: TestTask): FiniteDuration =
    Timing.calculateTime(Difficulty(task.difficulty), _workFactor)

  /** Расчитать время отдыха */
  def calculateRestTime(task: TestTask): FiniteDuration =
    Timing.calculateTime(Difficulty(task.difficulty), _restFactor)

}
