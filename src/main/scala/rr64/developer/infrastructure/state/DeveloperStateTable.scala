package rr64.developer.infrastructure.state

import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.state.DeveloperStateTable.stateMapper
import slick.ast.BaseTypedType
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

class DeveloperStateTable(tag: Tag) extends Table[(String, DeveloperState)](tag, "dev_state") {
  def id: Rep[String] = column[String]("id", O.PrimaryKey)
  def state: Rep[DeveloperState] = column[DeveloperState]("state")
  override def * : ProvenShape[(String, DeveloperState)] = (id, state)
}

object DeveloperStateTable {
  implicit val stateMapper: BaseTypedType[DeveloperState] =
    MappedColumnType.base[DeveloperState, String]({
      case DeveloperState.Free => "Free"
      case DeveloperState.Working => "Working"
      case DeveloperState.Resting => "Resting"
    }, {
      case "Free" => DeveloperState.Free
      case "Working" => DeveloperState.Working
      case "Resting" => DeveloperState.Resting
    })
}