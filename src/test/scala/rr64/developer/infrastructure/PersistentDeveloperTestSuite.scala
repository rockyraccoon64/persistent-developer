package rr64.developer.infrastructure

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain._

class PersistentDeveloperTestSuite extends AnyFlatSpec with Matchers {

  /** Разработчик начинает в свободном состоянии */
  "The developer" should "start in a free state" in {
    val developer = PersistentDeveloper()
    val state = developer.state
    state shouldEqual DeveloperState.Free
  }

}
