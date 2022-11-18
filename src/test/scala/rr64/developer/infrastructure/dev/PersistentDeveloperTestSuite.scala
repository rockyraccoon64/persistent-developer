package rr64.developer.infrastructure.dev

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Scheduler}
import org.scalatest.flatspec.AsyncFlatSpecLike
import rr64.developer.domain.{DeveloperReply, DeveloperState, Task}
import rr64.developer.infrastructure.dev.PersistentDeveloper.DeveloperRef
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior.Command
import rr64.developer.infrastructure.dev.behavior.Replies

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class PersistentDeveloperTestSuite
  extends ScalaTestWithActorTestKit
    with AsyncFlatSpecLike {

  private implicit val ec: ExecutionContext = testKit.system.executionContext
  private implicit val scheduler: Scheduler = testKit.system.scheduler

  private val emptyProvider = new DeveloperStateProvider {
    override def state(implicit ec: ExecutionContext): Future[DeveloperState] =
      Future.failed(new NotImplementedError)
  }

  private val emptyRef = testKit.spawn(Behaviors.empty[Command])

  private def mockDeveloperRef(
    receive: Command => Behavior[Command]
  ): DeveloperRef = {
    val mockBehavior = Behaviors.receiveMessage(receive)
    testKit.spawn(mockBehavior)
  }

  private def mockProvider(stateResult: DeveloperState): DeveloperStateProvider =
    (_: ExecutionContext) => Future.successful(stateResult)

  private def createDeveloper(
    developerRef: DeveloperRef = emptyRef,
    provider: DeveloperStateProvider = emptyProvider
  ) = PersistentDeveloper(developerRef, provider)

  /** Команда добавления задачи должна перенаправляться персистентному актору */
  "The Add Task command" should "be redirected to the persistent actor" in {
    val probe = testKit.createTestProbe[Command]()
    val dev = createDeveloper(probe.ref)

    val task = Task(10)
    dev.addTask(task)

    val command = probe.expectMessageType[Command.AddTask]
    command.task shouldEqual task
  }

  /** Когда разработчик отвечает "Задача начата", должно приходить соответствующее доменное сообщение */
  "When a task is started, there" should "be a corresponding domain message" in {
    val id = UUID.randomUUID()
    val mockActor = mockDeveloperRef {
      case Command.AddTask(_, replyTo) =>
        replyTo ! Replies.TaskStarted(id)
        Behaviors.same
    }
    val dev = createDeveloper(mockActor)

    val task = Task(15)
    val replyFuture = dev.addTask(task)

    replyFuture.map { _ shouldEqual DeveloperReply.TaskStarted(id) }
  }

  /** Когда разработчик отвечает "Задача поставлена в очередь", приходит соответствующее доменное сообщение */
  "When a task is queued, there" should "be a corresponding domain message" in {
    val id = UUID.randomUUID()
    val mockActor = mockDeveloperRef {
      case Command.AddTask(_, replyTo) =>
        replyTo ! Replies.TaskQueued(id)
        Behaviors.same
    }
    val dev = createDeveloper(mockActor)

    val task = Task(45)
    val replyFuture = dev.addTask(task)

    replyFuture.map { _ shouldEqual DeveloperReply.TaskQueued(id) }
  }

  /** Запрос на получение состояния разработчика перенаправляется провайдеру */
  "The State query" should "be redirected to the developer state provider" in {
    val working = DeveloperState.Working
    val free = DeveloperState.Free

    val provider1 = mockProvider(working)
    val provider2 = mockProvider(free)
    val dev1 = createDeveloper(provider = provider1)
    val dev2 = createDeveloper(provider = provider2)

    for {
      state1 <- dev1.state
      state2 <- dev2.state
    } yield {
      state1 shouldEqual working
      state2 shouldEqual free
    }
  }

}
