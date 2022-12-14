package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.ActorSystem
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import akka.persistence.typed.PersistenceId
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers._
import rr64.developer.domain.task.Task
import rr64.developer.domain.timing.Factor
import rr64.developer.infrastructure.DeveloperEventTestFacade.Event

class TestDeveloper(workFactor: Int, restFactor: Int)
                   (implicit system: ActorSystem[_]) {

  private type Kit = EventSourcedBehaviorTestKit[Command, Event, State]

  private val _workFactor = Factor(10)
  private val _restFactor = Factor(5)
  private val developerTestKit: Kit =
    EventSourcedBehaviorTestKit(
      system = system,
      behavior = DeveloperBehavior(
        persistenceId = PersistenceId.ofUniqueId("dev-test"),
        workFactor = _workFactor,
        restFactor = _restFactor
      ),
      SerializationSettings.disabled
    )

  def shouldBeFree: Assertion =
    developerTestKit.getState() shouldEqual State.Free

  def addTask(task: TestTask): Unit =
    developerTestKit.runCommand(Command.AddTask(task.toDomain, _))

  def shouldBeWorkingOnTask(task: TestTask): Assertion = {
    inside(developerTestKit.getState()) {
      case State.Working(currentTask, _) =>
      currentTask.task shouldEqual task.toDomain
    }
  }

}

case class TestTask(difficulty: Int) {
  def toDomain: Task = Task(difficulty)
}