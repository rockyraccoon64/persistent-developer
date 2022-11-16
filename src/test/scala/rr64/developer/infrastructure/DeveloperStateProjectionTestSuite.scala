package rr64.developer.infrastructure

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.projection.testkit.scaladsl.ProjectionTestKit

class DeveloperStateProjectionTestSuite
  extends ScalaTestWithActorTestKit {

  private val projectionTestKit = ProjectionTestKit(system)

}
