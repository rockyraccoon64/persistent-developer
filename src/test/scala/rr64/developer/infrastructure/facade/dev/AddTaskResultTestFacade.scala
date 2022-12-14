package rr64.developer.infrastructure.facade.dev

import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._
import rr64.developer.infrastructure.dev.behavior.{Command, Replies, State}
import rr64.developer.infrastructure.facade.dev.AddTaskResultTestFacade.AddTaskCommandResult
import rr64.developer.infrastructure.facade.event.DeveloperEventTestFacade.Event
import rr64.developer.infrastructure.facade.task.TestTaskIdentifier

import java.util.UUID

/**
 * Тестовый фасад результата поручения задачи разработчику
 * */
class AddTaskResultTestFacade private[facade](result: AddTaskCommandResult) {

  /** Проверить, что задача поставлена в очередь */
  def taskShouldBeQueued: Assertion =
    result.reply shouldBe a [Replies.TaskQueued]

  /** Проверить, что задача начата */
  def taskShouldBeStarted: Assertion =
    result.reply shouldBe a [Replies.TaskStarted]

  /** Проверить, что задаче присвоен идентификатор */
  def taskShouldHaveIdentifier: Assertion =
    extractId should not be null

  /** Идентификатор задачи */
  def taskId: TestTaskIdentifier =
    new TestTaskIdentifier(extractId)

  /** Извлечь идентификатор, присвоенный задаче */
  private def extractId: UUID = result.reply match {
    case Replies.TaskStarted(id) => id
    case Replies.TaskQueued(id) => id
  }

}

object AddTaskResultTestFacade {

  /** Результат поручения задачи разработчику */
  private[facade] type AddTaskCommandResult =
    EventSourcedBehaviorTestKit.CommandResultWithReply[
      Command,
      Event,
      State,
      Replies.AddTaskResult
    ]

}
