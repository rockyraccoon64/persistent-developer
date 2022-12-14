package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.ActorSystem
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import akka.persistence.typed.PersistenceId
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
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

}
