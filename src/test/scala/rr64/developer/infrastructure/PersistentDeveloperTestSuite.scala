package rr64.developer.infrastructure

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.Scheduler
import org.scalatest.flatspec.AnyFlatSpecLike
import rr64.developer.domain.Task

class PersistentDeveloperTestSuite
  extends ScalaTestWithActorTestKit
    with AnyFlatSpecLike {

  private implicit val scheduler: Scheduler = testKit.system.scheduler

  /** Команда добавления задачи должна перенаправляться персистентному актору */
  "The Add Task command" should "be redirected to the persistent actor" in {
    val probe = testKit.createTestProbe[DeveloperBehavior.Command]()
    val dev = PersistentDeveloper(probe.ref)

    val task = Task(10)
    dev.addTask(task)

    val command = probe.expectMessageType[DeveloperBehavior.AddTask]
    command.task shouldEqual task
  }

}
