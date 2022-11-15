package rr64.developer.infrastructure

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.Task
import rr64.developer.infrastructure.DeveloperBehavior._

class DeveloperBehaviorTestSuite extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config)
  with AnyFlatSpecLike
  with BeforeAndAfterEach
  with Matchers {

  private val eventSourcedTestKit =
    EventSourcedBehaviorTestKit[
      DeveloperBehavior.Command,
      DeveloperBehavior.Event,
      DeveloperBehavior.State
    ](
      system = system,
      behavior = DeveloperBehavior(),
      SerializationSettings.disabled
    )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    eventSourcedTestKit.clear()
  }

  /** Разработчик начинает в свободном состоянии */
  "The developer" should "start in a free state" in {
    val state = eventSourcedTestKit.getState()
    state shouldEqual DeveloperBehavior.Free
  }

  /** Когда разработчик свободен, он принимает задачу */
  "The developer" should "accept the task he's given when he's free" in {
    val task = Task()
    val result = eventSourcedTestKit.runCommand(AddTask(task, _))
    result.reply shouldEqual Replies.TaskAdded
  }

  /** Когда разработчик получает задачу, его состояние меняется на "Работает" */
  "The developer" should "start working when he's given a task" in {
    val task = Task()
    val result = eventSourcedTestKit.runCommand(AddTask(task, _))
    result.state shouldEqual State.Working
  }

}
