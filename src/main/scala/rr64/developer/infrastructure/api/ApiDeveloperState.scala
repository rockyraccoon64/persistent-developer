package rr64.developer.infrastructure.api

case class ApiDeveloperState private(state: String)

object ApiDeveloperState {
  val Working: ApiDeveloperState = ApiDeveloperState("Working")
}