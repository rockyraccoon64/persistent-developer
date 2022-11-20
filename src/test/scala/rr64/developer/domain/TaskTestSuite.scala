package rr64.developer.domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TaskTestSuite extends AnyWordSpec with Matchers {

  "Tasks" should {

    /** У задач должна быть сложность больше нуля */
    "only be allowed to have positive difficulty" in {
      assertThrows[IllegalArgumentException](Task(0))
      assertThrows[IllegalArgumentException](Task(-1))
      assertThrows[IllegalArgumentException](Task(-55))
      noException should be thrownBy Task(1)
      noException should be thrownBy Task(5)
    }
  }

}
