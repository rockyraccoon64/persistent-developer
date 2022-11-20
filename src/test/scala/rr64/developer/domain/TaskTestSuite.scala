package rr64.developer.domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TaskTestSuite extends AnyWordSpec with Matchers {

  "Tasks" should {

    /** Сложность задач должна быть больше нуля */
    "only be allowed to have positive difficulty" in {
      assertThrows[IllegalArgumentException](Task(0))
      assertThrows[IllegalArgumentException](Task(-1))
      assertThrows[IllegalArgumentException](Task(-55))
      noException should be thrownBy Task(1)
      noException should be thrownBy Task(5)
    }

    /** Сложность задач должна быть меньше или равна 100 */
    "only be allowed to have difficulty less than or equal to 100" in {
      assertThrows[IllegalArgumentException](Task(12345))
      assertThrows[IllegalArgumentException](Task(101))
      noException should be thrownBy Task(100)
      noException should be thrownBy Task(55)
    }
  }

}
