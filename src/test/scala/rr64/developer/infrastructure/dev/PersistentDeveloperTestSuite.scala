package rr64.developer.infrastructure.dev

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Scheduler}
import org.scalamock.scalatest.MockFactory
import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpecLike
import rr64.developer.domain.dev.{DeveloperReply, DeveloperState}
import rr64.developer.domain.task.Task
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior.DeveloperRef
import rr64.developer.infrastructure.dev.behavior.{Command, Replies}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Тесты реализации разработчика на основе актора и источника состояний
 */
class PersistentDeveloperTestSuite
  extends ScalaTestWithActorTestKit
    with AsyncFlatSpecLike
    with MockFactory {

  private implicit val ec: ExecutionContext = testKit.system.executionContext
  private implicit val scheduler: Scheduler = testKit.system.scheduler

  /** Источник состояний без поведения */
  private val emptyProvider = new DeveloperStateProvider {
    override def state(implicit ec: ExecutionContext): Future[DeveloperState] =
      Future.failed(new NotImplementedError)
  }

  /** Актор без поведения */
  private val emptyRef = testKit.spawn(Behaviors.empty[Command])

  /** Заглушка актора на основе переданного обработчика сообщений */
  private def mockDeveloperRef(receive: Command => Behavior[Command]): DeveloperRef = {
    val mockBehavior = Behaviors.receiveMessage(receive)
    testKit.spawn(mockBehavior)
  }

  /** Создать PersistentDeveloper на основе актора и источника состояний */
  private def createDeveloper(
    developerRef: DeveloperRef = emptyRef,
    provider: DeveloperStateProvider = emptyProvider
  ) = PersistentDeveloper(developerRef, provider)

  /** Проверить, что запрос на получение состояния перенаправляется провайдеру */
  private def assertQueryRedirected(state: DeveloperState): Assertion = {
    val mockProvider = mock[DeveloperStateProvider]
    (mockProvider.state(_: ExecutionContext))
      .expects(*)
      .once()
      .returning(Future.successful(state))

    val dev = createDeveloper(provider = mockProvider)
    dev.state.futureValue shouldEqual state
  }

  /** Команда добавления задачи должна перенаправляться персистентному актору */
  "The Add Task command" should "be redirected to the persistent actor" in {
    val probe = testKit.createTestProbe[Command]()
    val dev = createDeveloper(probe.ref)

    val task = Task(10)
    dev.addTask(task)

    val command = probe.expectMessageType[Command.AddTask]
    command.task shouldEqual task
  }

  /** Когда разработчик отвечает "Задача начата",
   * должно приходить соответствующее доменное сообщение */
  "When a task is started, there" should "be a corresponding domain message" in {
    val id = UUID.fromString("cd9e1104-aff1-4085-b740-463f79376638")
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

  /** Когда разработчик отвечает "Задача поставлена в очередь",
   * приходит соответствующее доменное сообщение */
  "When a task is queued, there" should "be a corresponding domain message" in {
    val id = UUID.fromString("3427ee7f-6e97-4e60-bca9-ba10c29e33bc")
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
    assertQueryRedirected(DeveloperState.Free)
    assertQueryRedirected(DeveloperState.Working)
    assertQueryRedirected(DeveloperState.Resting)
  }

}
