package rr64.developer.infrastructure.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalamock.scalatest.MockFactory
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.domain._

import scala.concurrent.{ExecutionContext, Future}

class RestApiTests
  extends AnyWordSpec
    with Matchers
    with ScalatestRouteTest
    with MockFactory {

  private val service = mock[DeveloperService]
  private val route = new RestApi(service).route

  "The service" should {

    /** Запрос состояния разработчика */
    "return the developer state" in {
      def checkState(domainState: DeveloperState, expectedApiState: ApiDeveloperState): Assertion = {
        (service.developerState(_: ExecutionContext))
          .expects(*)
          .returning(
            Future.successful(domainState)
          )
        Get("/api/query/developer-state") ~> route ~> check {
          responseAs[ApiDeveloperState] shouldEqual expectedApiState
        }
      }
      checkState(DeveloperState.Free, ApiDeveloperState.Free)
      checkState(DeveloperState.Working, ApiDeveloperState.Working)
      checkState(DeveloperState.Resting, ApiDeveloperState.Resting)
    }

  }

}
