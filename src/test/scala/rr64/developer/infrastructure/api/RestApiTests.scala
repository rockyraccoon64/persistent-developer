package rr64.developer.infrastructure.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalamock.scalatest.MockFactory
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

  private def checkState(actual: DeveloperState, expected: ApiDeveloperState) = {
    (service.developerState(_: ExecutionContext))
      .expects(*)
      .returning(
        Future.successful(actual)
      )
    Get("/api/query/developer-state") ~> route ~> check {
      responseAs[ApiDeveloperState] shouldEqual expected
    }
  }

  "The service" should {

    /** Запрос состояния разработчика, когда он свободен */
    "return the Free developer state" in {
      checkState(DeveloperState.Free, ApiDeveloperState.Free)
    }

    /** Запрос состояния разработчика, когда он работает */
    "return the Working developer state" in {
      checkState(DeveloperState.Working, ApiDeveloperState.Working)
    }

  }

}
