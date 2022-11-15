package rr64.developer.infrastructure

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain._

class PersistentDeveloperTestSuite extends AnyFlatSpec with Matchers {

  /** Когда разработчик свободен, он принимает задачу */
  "The developer" should "accept the task he's given when he's free" in {
    val developer = PersistentDeveloper()
    val task = Task()
    val reply = developer.addTask(task)
    reply shouldEqual DeveloperReply.TaskAccepted
  }

  /** Когда разработчик получает задачу, его состояние меняется на "Работает" */
  "The developer" should "start working when he's given a task" in {
    val developer = PersistentDeveloper()
    val task = Task()
    developer.addTask(task)
    developer.state shouldEqual DeveloperState.Working
  }

}
