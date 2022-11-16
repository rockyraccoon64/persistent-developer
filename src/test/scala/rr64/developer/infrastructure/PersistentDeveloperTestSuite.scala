package rr64.developer.infrastructure

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import org.scalatest.flatspec.AsyncFlatSpecLike
import rr64.developer.domain.{DeveloperReply, Task}

import java.util.UUID
import scala.concurrent.ExecutionContext

class PersistentDeveloperTestSuite
  extends ScalaTestWithActorTestKit
    with AsyncFlatSpecLike {

  private implicit val ec: ExecutionContext = testKit.system.executionContext
  private implicit val scheduler: Scheduler = testKit.system.scheduler

  private def mockActorRef(
    receive: DeveloperBehavior.Command => Behavior[DeveloperBehavior.Command]
  ): ActorRef[DeveloperBehavior.Command] = {
    val mockBehavior = Behaviors.receiveMessage(receive)
    testKit.spawn(mockBehavior)
  }

  /** Команда добавления задачи должна перенаправляться персистентному актору */
  "The Add Task command" should "be redirected to the persistent actor" in {
    val probe = testKit.createTestProbe[DeveloperBehavior.Command]()
    val dev = PersistentDeveloper(probe.ref)

    val task = Task(10)
    dev.addTask(task)

    val command = probe.expectMessageType[DeveloperBehavior.AddTask]
    command.task shouldEqual task
  }

  /** Когда разработчик отвечает "Задача начата", должно приходить соответствующее доменное сообщение */
  "When a task is started, there" should "be a corresponding domain message" in {
    val id = UUID.randomUUID()
    val mockActor = mockActorRef {
      case DeveloperBehavior.AddTask(_, replyTo) =>
        replyTo ! DeveloperBehavior.Replies.TaskStarted(id)
        Behaviors.same
    }
    val dev = PersistentDeveloper(mockActor)

    val task = Task(15)
    val replyFuture = dev.addTask(task)

    replyFuture.map { _ shouldEqual DeveloperReply.TaskStarted(id) }
  }

  /** Когда разработчик отвечает "Задача поставлена в очередь", приходит соответствующее доменное сообщение */
  "When a task is queued, there" should "be a corresponding domain message" in {
    val id = UUID.randomUUID()
    val mockActor = mockActorRef {
      case DeveloperBehavior.AddTask(_, replyTo) =>
        replyTo ! DeveloperBehavior.Replies.TaskQueued(id)
        Behaviors.same
    }
    val dev = PersistentDeveloper(mockActor)

    val task = Task(45)
    val replyFuture = dev.addTask(task)

    replyFuture.map { _ shouldEqual DeveloperReply.TaskQueued(id) }
  }

}
