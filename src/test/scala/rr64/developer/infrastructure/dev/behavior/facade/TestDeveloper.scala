package rr64.developer.infrastructure.dev.behavior.facade

import akka.actor.typed.ActorSystem
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import akka.persistence.typed.PersistenceId
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers._
import rr64.developer.domain.task.Task
import rr64.developer.domain.timing.Factor
import rr64.developer.infrastructure.DeveloperEventTestFacade.{Event, taskStartedEvent}
import rr64.developer.infrastructure.dev.behavior.facade.TestAddTaskResult.AddTaskCommandResult
import rr64.developer.infrastructure.dev.behavior.{Command, DeveloperBehavior, Replies, State}
import rr64.developer.infrastructure.task.TaskWithId

import java.util.UUID

class TestDeveloper(workFactor: Int, restFactor: Int)
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

  def shouldBeFree: Assertion =
    developerTestKit.getState() shouldEqual State.Free

  def shouldBeWorkingOnTask(task: TestTask): Assertion =
    inside(developerTestKit.getState()) {
      case working: State.Working =>
        working.currentTask.task shouldEqual task.toDomain
    }

  def shouldNotBeWorking: Assertion =
    developerTestKit.getState() should not be a [State.Working]

  def shouldRestAfterCompletingTask(task: TestTask): Assertion =
    inside(developerTestKit.getState()) {
      case resting: State.Resting =>
        resting.lastCompleted.task shouldEqual task.toDomain
    }

  def shouldNotBeResting: Assertion =
    developerTestKit.getState() should not be a [State.Resting]

  def afterStartingTask(task: TaskWithId): Unit = // TODO Убрать TaskWithId
    developerTestKit.initialize(taskStartedEvent(task))

  def addTask(task: TestTask): TestAddTaskResult = {
    val result = developerTestKit.runCommand(Command.AddTask(task.toDomain, _))
    new TestAddTaskResult(result)
  }

  def addsTaskAndRepliesWithIdentifier(newTask: TestTask): Assertion = {
    val result = addTask(newTask)
    result.isIdAssignedAfterQueueing
  }

  def workingOnTaskWithReturnedIdentifier(result: TestAddTaskResult): Assertion = {
    inside(developerTestKit.getState()) {
      case working: State.Working =>
        working.currentTask.id shouldEqual result.id
    }
  }

}

class TestAddTaskResult private[facade](result: AddTaskCommandResult) {

  def isQueued: Assertion =
    result.reply shouldBe a [Replies.TaskQueued]

  def isIdAssignedAfterQueueing: Assertion = {
    val reply = result.replyOfType[Replies.TaskQueued]
    reply.id should not be null
  }

  def taskShouldBeStarted: Assertion =
    result.reply shouldBe a [Replies.TaskStarted]

  def identifierAssignedAfterStarting: Assertion =
    result.replyOfType[Replies.TaskStarted].id should not be null

  private[facade] def id: UUID =
    result.replyOfType[Replies.TaskStarted].id

}

object TestAddTaskResult {
  private[facade] type AddTaskCommandResult =
    EventSourcedBehaviorTestKit.CommandResultWithReply[
      Command,
      Event,
      State,
      Replies.AddTaskResult
    ]
}

case class TestTask(difficulty: Int) {
  private[facade] def toDomain: Task = Task(difficulty)
}