package rr64.developer.infrastructure.state
import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.state.DeveloperStateSlickRepository.states
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

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
      (implicit ec: ExecutionContext): Future[Option[DeveloperState]] =
    db.run {
      states.filter(_.id === id)
        .map(_.state)
        .take(1)
        .result
        .headOption
        .map(_.map {
          case "Free" => DeveloperState.Free
          case "Working" => DeveloperState.Working
          case "Resting" => DeveloperState.Resting
        })
    }


}

object DeveloperStateSlickRepository {

  private[state] class DeveloperStateTable(tag: Tag) extends Table[(String, String)](tag, "dev_state") {
    def id: Rep[String] = column[String]("id", O.PrimaryKey)
    def state: Rep[String] = column[String]("state")
    override def * : ProvenShape[(String, String)] = (id, state)
  }

  private[state] val states = TableQuery[DeveloperStateTable]

}