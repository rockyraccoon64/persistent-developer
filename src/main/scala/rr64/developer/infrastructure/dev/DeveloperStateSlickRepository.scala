package rr64.developer.infrastructure.dev

import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.dev.DeveloperStateSlickRepository._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Репозиторий состояний разработчиков на основе PostgreSQL + Slick
 * @param db База данных PostgreSQL
 * */
class DeveloperStateSlickRepository(db: Database) extends DeveloperStateRepository {

  override def save(id: String, state: DeveloperState)
      (implicit ec: ExecutionContext): Future[_] = {
    val name = state match {
      case DeveloperState.Free => FreeState
      case DeveloperState.Working => WorkingState
      case DeveloperState.Resting => RestingState
    }
    db.run {
      sqlu"""INSERT INTO dev_state (id, state)
            VALUES ($id, $name)
            ON CONFLICT (id)
            DO UPDATE SET state = $name"""
    }
  }

  override def findById(id: String)
      (implicit ec: ExecutionContext): Future[Option[DeveloperState]] = {
    db.run {
      sql"""SELECT state FROM dev_state WHERE id = $id"""
        .as[String]
        .headOption
        .map(_.map {
          case FreeState => DeveloperState.Free
          case WorkingState => DeveloperState.Working
          case RestingState => DeveloperState.Resting
        })
    }
  }

}

object DeveloperStateSlickRepository {
  private val FreeState = "Free"
  private val WorkingState = "Working"
  private val RestingState = "Resting"
}