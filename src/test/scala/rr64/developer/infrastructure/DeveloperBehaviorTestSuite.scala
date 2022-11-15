package rr64.developer.infrastructure

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import akka.persistence.typed.PersistenceId
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.Task
import rr64.developer.infrastructure.DeveloperBehavior._

class DeveloperBehaviorTestSuite extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config)
  with AnyFlatSpecLike
  with BeforeAndAfterEach
  with Matchers {

  private val developerTestKit =
    EventSourcedBehaviorTestKit[
      DeveloperBehavior.Command,
      DeveloperBehavior.Event,
      DeveloperBehavior.State
    ](
      system = system,
      behavior = DeveloperBehavior(PersistenceId.ofUniqueId("dev-test")),
      SerializationSettings.disabled
    )

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
    val task = Task()
    val result = developerTestKit.runCommand(AddTask(task, _))
    result.reply shouldEqual Replies.TaskStarted
    result.event shouldEqual Event.TaskAdded
    result.state shouldEqual State.Working
  }

  /** По окончании выполнения задачи разработчик снова свободен */
  "The developer" should "stop working when the task is done" in {
    val difficulty = 30
    val factor = 10
    val time = difficulty * factor
    val task = Task(difficulty)
    val result = developerTestKit.runCommand(AddTask(task, _))
    Thread.sleep(time) // TODO
    developerTestKit.getState() shouldEqual State.Free
  }

}
