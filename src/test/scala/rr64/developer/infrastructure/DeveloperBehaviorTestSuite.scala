package rr64.developer.infrastructure

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import akka.persistence.typed.PersistenceId
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.Task
import rr64.developer.infrastructure.DeveloperBehavior.State.Working
import rr64.developer.infrastructure.DeveloperBehavior._

class DeveloperBehaviorTestSuite extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config)
  with AnyFlatSpecLike
  with BeforeAndAfterEach
  with Matchers {

  def testKit(persistenceId: String, timeFactor: Int): EventSourcedBehaviorTestKit[
    DeveloperBehavior.Command,
    DeveloperBehavior.Event,
    DeveloperBehavior.State
  ] = {
    EventSourcedBehaviorTestKit(
      system = system,
      behavior = DeveloperBehavior(
        persistenceId = PersistenceId.ofUniqueId(persistenceId),
        timeFactor = timeFactor
      ),
      SerializationSettings.disabled
    )
  }

  private val timeFactor = 10
  private val developerTestKit = testKit("dev-test", timeFactor)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    developerTestKit.clear()
  }

  /** Разработчик начинает в свободном состоянии */
  "The developer" should "start in a free state" in {
    val state = developerTestKit.getState()
    state shouldEqual DeveloperBehavior.State.Free
  }

  /** Когда разработчик свободен, он принимает задачу в работу */
  "The developer" should "accept the task he's given when he's free" in {
    val task = Task(5)
    val result = developerTestKit.runCommand(AddTask(task, _))
    result.reply shouldEqual Replies.TaskStarted
    result.event shouldEqual Event.TaskStarted(task)
    val state = result.stateOfType[Working]
    state.task shouldEqual task
  }

  /** До выполнения задачи разработчик работает, по окончании снова свободен */
  "The developer" should "work until the task is done" in {
    val factor = 100
    val difficulty = 10
    val task = Task(difficulty)
    val firstCheckMs = 750
    val secondCheckMs = 500
    val kit = testKit("timer-test", factor)

    kit.runCommand(AddTask(task, _))

    Thread.sleep(firstCheckMs)
    kit.getState() shouldEqual State.Working(task)

    Thread.sleep(secondCheckMs)
    kit.getState() should not equal State.Working(task)
  }

  /** Завершив задачу, разработчик делает перерыв */
  "The developer" should "rest after completing a task" in {
    val difficulty = 50
    val task = Task(difficulty)
    val workTime = difficulty * timeFactor
    val restingTime = difficulty * 100 // TODO Тоже в инициализацию Behavior

    developerTestKit.runCommand(AddTask(task, _))

    Thread.sleep(workTime + 100)
    developerTestKit.getState() shouldEqual State.Resting(restingTime)

    Thread.sleep(restingTime)
    developerTestKit.getState() shouldEqual State.Free
  }

}
