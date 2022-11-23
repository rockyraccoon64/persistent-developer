package rr64.developer.infrastructure.dev

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import rr64.developer.domain.dev.DeveloperState

import scala.concurrent.{ExecutionContext, Future}

/**
 * Тесты источника состояний разработчиков на основе репозитория
 */
class DeveloperStateFromRepositoryTestSuite extends AsyncWordSpec with Matchers {

  private val dev1 = "walter"
  private val dev2 = "mark"
  private val dev3 = "gruff97"

  /** Репозиторий, из которого берутся состояния */
  private val mockRepository: DeveloperStateRepository =
    new DeveloperStateRepository {
      private var states: Map[String, DeveloperState] = Map(
        dev1 -> DeveloperState.Working,
        dev2 -> DeveloperState.Resting,
        dev3 -> DeveloperState.Free
      )
      override def save(id: String, state: DeveloperState)
          (implicit ec: ExecutionContext): Future[Unit] = {
        states = states.updated(id, state)
        Future.unit
      }
      override def findById(id: String)
          (implicit ec: ExecutionContext): Future[Option[DeveloperState]] =
        Future.successful(states.get(id))
    }

  /** Создать источник состояний для конкретного разработчика */
  private def createProvider(developerId: String) =
    new DeveloperStateFromRepository(developerId, mockRepository)

  /** Создать источник состояний и проверить текущее состояние */
  private def assertState(developerId: String, state: DeveloperState): Future[Assertion] = {
    val provider = createProvider(developerId)
    provider.state.map(_ shouldEqual state)
  }

  /** Источник состояний разработчика */
  "The developer state provider" should {

    /** Должен извлекать известное состояние разработчика из репозитория */
    "extract an existing state for the given developer id from the repository" in {
      for {
        _ <- assertState(dev1, DeveloperState.Working)
        _ <- assertState(dev2, DeveloperState.Resting)
        _ <- assertState(dev3, DeveloperState.Free)
      } yield succeed
    }

    /** Если состояние разработчика не сохранено в репозитории,
     * то он ещё не получил задачу и находится в начальном состоянии */
    "return the initial state if the developer's state is not saved in the repository" in {
      val nonexistentDevId = "dave"
      assertState(nonexistentDevId, DeveloperState.Free)
    }

  }

}
