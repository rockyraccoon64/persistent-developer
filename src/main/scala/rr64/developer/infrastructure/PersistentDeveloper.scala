package rr64.developer.infrastructure

import rr64.developer.domain._
import rr64.developer.domain.DeveloperState._

class PersistentDeveloper extends Developer {
  def state: DeveloperState = Free
}

object PersistentDeveloper {
  def apply(): PersistentDeveloper = new PersistentDeveloper
}
