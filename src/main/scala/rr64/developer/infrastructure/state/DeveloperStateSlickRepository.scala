package rr64.developer.infrastructure.state
import rr64.developer.domain.DeveloperState
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class DeveloperStateSlickRepository(db: Database) extends DeveloperStateRepository {

  override def save(id: String, state: DeveloperState)
      (implicit ec: ExecutionContext): Future[_] = {
    val name = state match {
      case DeveloperState.Free => "Free"
      case DeveloperState.Working => "Working"
      case DeveloperState.Resting => "Resting"
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
          case "Free" => DeveloperState.Free
          case "Working" => DeveloperState.Working
          case "Resting" => DeveloperState.Resting
        })
    }
  }

}
