package rr64.developer.infrastructure

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import akka.persistence.typed.PersistenceId
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.Task
import rr64.developer.infrastructure.DeveloperBehavior.State.Working
import rr64.developer.infrastructure.DeveloperBehavior._

class DeveloperBehaviorTestSuite extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config)
  with AnyFlatSpecLike
  with BeforeAndAfterEach
  with Matchers {

  def testKit(
    persistenceId: String,
    timeFactor: Int,
    restFactor: Int
  ): EventSourcedBehaviorTestKit[
    DeveloperBehavior.Command,
    DeveloperBehavior.Event,
    DeveloperBehavior.State
  ] = {
    EventSourcedBehaviorTestKit(
      system = system,
      behavior = DeveloperBehavior(
        persistenceId = PersistenceId.ofUniqueId(persistenceId),
        timeFactor = timeFactor,
        restFactor = restFactor
      ),
      SerializationSettings.disabled
    )
  }

  private val timeFactor = 10
  private val restFactor = 5
  private val developerTestKit = testKit("dev-test", timeFactor, restFactor)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    developerTestKit.clear()
  }

  /** Разработчик начинает в свободном состоянии */
  "The developer" should "start in a free state" in {
    val state = developerTestKit.getState()
    state shouldBe a [State.Free]
  }

  /** Когда разработчик свободен, он принимает задачу в работу */
  "The developer" should "accept the task he's given when he's free" in {
    val task = Task(5)
    val result = developerTestKit.runCommand(AddTask(task, _))
    val state = result.stateOfType[Working]
    state.task shouldEqual task
  }

  /** Когда разработчик свободен, то при получении задачи
   * он присваивает ей идентификатор и отправляет его в ответе */
  "The developer" should "reply with a Task Started message when he's free" in {
    val task = Task(5)
    val result = developerTestKit.runCommand(AddTask(task, _))
    val reply = result.replyOfType[Replies.TaskStarted]
    reply.id should not be null
    result.stateOfType[Working].taskId shouldEqual reply.id
  }

  /** До выполнения задачи разработчик работает, по окончании снова свободен */
  "The developer" should "work until the task is done" in {
    val workFactor = 100
    val restFactor = 5
    val difficulty = 10
    val task = Task(difficulty)
    val firstCheckMs = 750
    val secondCheckMs = 500
    val kit = testKit("timer-test", workFactor, restFactor)

    kit.runCommand(AddTask(task, _))

    Thread.sleep(firstCheckMs)

    inside(kit.getState()) {
      case working: Working => working.task shouldEqual task
    }

    Thread.sleep(secondCheckMs)

    kit.getState() should not be a [State.Working]
  }

  /** Завершив задачу, разработчик делает перерыв */
  "The developer" should "rest after completing a task" in {
    val difficulty = 50
    val task = Task(difficulty)
    val workTime = difficulty * timeFactor
    val restingTime = difficulty * restFactor

    developerTestKit.runCommand(AddTask(task, _))

    Thread.sleep(workTime + 100)

    inside(developerTestKit.getState()) {
      case State.Resting(millis) => millis shouldEqual restingTime
    }

    Thread.sleep(restingTime)

    developerTestKit.getState() shouldBe a [State.Free]
  }

}
