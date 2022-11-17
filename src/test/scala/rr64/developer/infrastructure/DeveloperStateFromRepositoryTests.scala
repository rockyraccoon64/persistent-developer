package rr64.developer.infrastructure

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.state.{DeveloperStateFromRepository, DeveloperStateRepository}

import scala.concurrent.{ExecutionContext, Future}

class DeveloperStateFromRepositoryTests extends AsyncFlatSpec with Matchers { // TODO В пакет state

  /** Источник должен извлекать известное состояние разработчика из репозитория */
  "The provider" should "extract an existing state for the given developer id from the repository" in {
    val developerId = "dev-id1"
    val mockRepository = new DeveloperStateRepository {
      private var states: Map[String, DeveloperState] = Map(
        developerId -> DeveloperState.Working,
        "dev-id2" -> DeveloperState.Resting,
        "dev-id3" -> DeveloperState.Free
      )
      override def save(id: String, state: DeveloperState)(implicit ec: ExecutionContext): Future[Unit] = {
        states = states.updated(id, state)
        Future.unit
      }
      override def findById(id: String)(implicit ec: ExecutionContext): Future[Option[DeveloperState]] =
        Future.successful(states.get(id))
    }
    val provider = new DeveloperStateFromRepository(developerId, mockRepository)

    provider.state.map(_ shouldEqual DeveloperState.Working)
  }

}
