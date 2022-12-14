package rr64.developer.infrastructure.dev.behavior.facade

import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._
import rr64.developer.infrastructure.DeveloperEventTestFacade.Event
import rr64.developer.infrastructure.dev.behavior.facade.TestAddTaskResult.AddTaskCommandResult
import rr64.developer.infrastructure.dev.behavior.{Command, Replies, State}

import java.util.UUID

class TestAddTaskResult private[facade](result: AddTaskCommandResult) {

  def taskShouldBeQueued: Assertion =
    result.reply shouldBe a [Replies.TaskQueued]

  def taskShouldBeStarted: Assertion =
    result.reply shouldBe a [Replies.TaskStarted]

  def taskShouldHaveIdentifier: Assertion =
    extractId should not be null

  def id: TestTaskIdentifier =
    new TestTaskIdentifier(extractId)

  private def extractId: UUID = result.reply match {
    case Replies.TaskStarted(id) => id
    case Replies.TaskQueued(id) => id
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
