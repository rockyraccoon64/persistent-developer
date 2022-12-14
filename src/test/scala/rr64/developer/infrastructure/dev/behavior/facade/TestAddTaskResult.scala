package rr64.developer.infrastructure.dev.behavior.facade

import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._
import rr64.developer.infrastructure.DeveloperEventTestFacade.Event
import rr64.developer.infrastructure.dev.behavior.facade.TestAddTaskResult.AddTaskCommandResult
import rr64.developer.infrastructure.dev.behavior.{Command, Replies, State}

class TestAddTaskResult private[facade](result: AddTaskCommandResult) {

  def taskShouldBeQueued: Assertion =
    result.reply shouldBe a [Replies.TaskQueued]

  def queuedTaskShouldBeAssignedId: Assertion = {
    val reply = result.replyOfType[Replies.TaskQueued]
    reply.id should not be null
  }

  def taskShouldBeStarted: Assertion =
    result.reply shouldBe a [Replies.TaskStarted]

  def startedTaskShouldBeAssignedId: Assertion =
    result.replyOfType[Replies.TaskStarted].id should not be null

  def id: TestTaskIdentifier = {
    val idResult = result.replyOfType[Replies.TaskStarted].id
    new TestTaskIdentifier(idResult)
  }

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
