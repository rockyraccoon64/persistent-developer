package rr64.developer.infrastructure.state

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.DeveloperState

import scala.concurrent.{ExecutionContext, Future}

class DeveloperStateFromRepositoryTests extends AsyncFlatSpec with Matchers {

  private val dev1 = "walter"
  private val dev2 = "mark"
  private val dev3 = "gruff97"

  private def mockRepository = new DeveloperStateRepository {
    private var states: Map[String, DeveloperState] = Map(
      dev1 -> DeveloperState.Working,
      dev2 -> DeveloperState.Resting,
      dev3 -> DeveloperState.Free
    )
    override def save(id: String, state: DeveloperState)(implicit ec: ExecutionContext): Future[Unit] = {
      states = states.updated(id, state)
      Future.unit
    }
    override def findById(id: String)(implicit ec: ExecutionContext): Future[Option[DeveloperState]] =
      Future.successful(states.get(id))
  }

  /** Источник должен извлекать известное состояние разработчика из репозитория */
  "The provider" should "extract an existing state for the given developer id from the repository" in {
    val provider = new DeveloperStateFromRepository(dev1, mockRepository)
    provider.state.map(_ shouldEqual DeveloperState.Working)
  }

  /** Если состояние разработчика не сохранено в репозитории,
   * он ещё не получил задачу и находится в начальном состоянии */
  "The provider" should "return the initial state if the developer's state is not saved in the repository" in {
    val nonexistentDevId = "dave"
    val provider = new DeveloperStateFromRepository(nonexistentDevId, mockRepository)
    provider.state.map(_ shouldEqual DeveloperState.Free)
  }

}
