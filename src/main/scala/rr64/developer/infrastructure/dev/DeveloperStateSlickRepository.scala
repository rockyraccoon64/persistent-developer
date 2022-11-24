package rr64.developer.infrastructure.dev

import rr64.developer.domain.dev.DeveloperState
import rr64.developer.infrastructure.Codec
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Репозиторий состояний разработчиков на основе PostgreSQL + Slick
 * @param db База данных PostgreSQL
 * @param codec Кодек состояния разработчика
 * */
class DeveloperStateSlickRepository(
  db: Database,
  codec: Codec[DeveloperState, String]
) extends DeveloperStateRepository {

  override def save(id: String, state: DeveloperState)
      (implicit ec: ExecutionContext): Future[_] = {
    val name = codec.encode(state)
    db.run {
      sqlu"""INSERT INTO dev_state (id, state)
            VALUES ($id, $name::dev_state_type)
            ON CONFLICT (id)
            DO UPDATE SET state = $name::dev_state_type"""
    }
  }

  override def findById(id: String)
      (implicit ec: ExecutionContext): Future[Option[DeveloperState]] = {
    db.run {
      sql"""SELECT state FROM dev_state WHERE id = $id"""
        .as[String]
        .headOption
        .map(_.map(codec.decode))
    }
  }

}
