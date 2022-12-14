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

/**
 * Тестовый фасад актора разработчика
 * */
class DeveloperTestFacade private(workFactor: Factor, restFactor: Factor)
    (implicit system: ActorSystem[_]) {

  /** Тестовый набор Akka Persistence */
  private val developerTestKit: EventSourcedBehaviorTestKit[Command, Event, State] =
    EventSourcedBehaviorTestKit(
      system = system,
      behavior = DeveloperBehavior(
        persistenceId = PersistenceId.ofUniqueId("dev-test"),
        workFactor = workFactor,
        restFactor = restFactor
      ),
      SerializationSettings.disabled
    )

  /** Вернуться в изначальное состояние */
  def reset(): Unit = developerTestKit.clear()

  /** Перезапустить актора */
  def restart(): Unit = developerTestKit.restart()

  /** Поручить задачу */
  def addTask(task: TestTask): AddTaskResultTestFacade = {
    val result = developerTestKit.runCommand(Command.AddTask(task.toDomain, _))
    new AddTaskResultTestFacade(result)
  }

  /** Проверить, что разработчик в состоянии "Свободен" */
  def shouldBeFree: Assertion =
    developerTestKit.getState() shouldEqual State.Free

  /** Проверить, что разработчик в процессе выполнения задачи */
  def shouldBeWorkingOnTask(task: TestTask): Assertion =
    inside(developerTestKit.getState()) {
      case working: State.Working =>
        working.currentTask.task shouldEqual task.toDomain
    }

  /** Проверить, что разработчик в процессе выполнения задачи с конкретным идентификатором */
  def shouldBeWorkingOnTaskWithId(id: TestTaskIdentifier): Assertion =
    inside(developerTestKit.getState()) {
      case working: State.Working =>
        working.currentTask.id shouldEqual id.id
    }

  /** Проверить, что разработчик не выполняет задачу в текущий момент */
  def shouldNotBeWorking: Assertion =
    developerTestKit.getState() should not be a [State.Working]

  /** Проверить, что разработчик отдыхает */
  def shouldBeResting: Assertion =
    developerTestKit.getState() shouldBe a [State.Resting]

  /** Проверить, что разработчик отдыхает после выполнения конкретной задачи */
  def shouldBeRestingAfterCompletingTask(task: TestTask): Assertion =
    inside(developerTestKit.getState()) {
      case resting: State.Resting =>
        resting.lastCompleted.task shouldEqual task.toDomain
    }

  /** Проверить, что разработчик не отдыхает в текущий момент */
  def shouldNotBeResting: Assertion =
    developerTestKit.getState() should not be a [State.Resting]

  /** Считать, что дальнейшие команды и запросы выполняются после начала работы над задачей */
  def afterStartingTask(task: TestTaskWithId): Unit =
    developerTestKit.initialize(taskStartedEvent(task.toDomain))

  /** Считать, что дальнейшие команды и запросы выполняются после завершения работы над задачей */
  def afterCompletingTask(task: TestTaskWithId): Unit = {
    val domainTask = task.toDomain
    developerTestKit.initialize(
      taskStartedEvent(domainTask),
      taskFinishedEvent(domainTask)
    )
  }

  /** Считать, что дальнейшие команды и запросы выполняются во время отдыха */
  def whileResting(lastCompleted: TestTaskWithId, taskQueue: Seq[TestTaskWithId]): Unit =
    developerTestKit.initialize(State.Resting(lastCompleted.toDomain, taskQueue.map(_.toDomain)))

  /** Проверить содержимое очереди */
  def queueShouldEqual(tasks: Seq[TestTask]): Assertion = {
    val queue = developerTestKit.getState() match {
      case working: State.Working => working.taskQueue
      case resting: State.Resting => resting.taskQueue
    }
    queue.map(_.task) should contain theSameElementsInOrderAs tasks.map(_.toDomain)
  }

  /** Расчитать время работы */
  def calculateWorkTime(task: TestTask): FiniteDuration =
    Timing.calculateTime(Difficulty(task.difficulty), workFactor)

  /** Расчитать время отдыха */
  def calculateRestTime(task: TestTask): FiniteDuration =
    Timing.calculateTime(Difficulty(task.difficulty), restFactor)

}

object DeveloperTestFacade {

  def apply(workFactor: Int, restFactor: Int)
      (implicit system: ActorSystem[_]): DeveloperTestFacade = {
    val _workFactor = Factor(workFactor)
    val _restFactor = Factor(restFactor)
    new DeveloperTestFacade(_workFactor, _restFactor)
  }

}