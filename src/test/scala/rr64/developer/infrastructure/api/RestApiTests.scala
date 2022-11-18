package rr64.developer.infrastructure.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalamock.matchers.MockParameter
import org.scalamock.scalatest.MockFactory
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.domain._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class RestApiTests
  extends AnyWordSpec
    with Matchers
    with ScalatestRouteTest
    with MockFactory {

  private val service = mock[DeveloperService]
  private val route = new RestApi(service).route

  /** Запрос информации о задаче */
  "The service processing a single task info request" should {

    val baseUrl = "/api/query/task-info"

    def mockExpects(id: MockParameter[UUID]) =
      (service.taskInfo(_: UUID)(_: ExecutionContext))
        .expects(id, *)


    /** Когда задача существует, возвращается информация о ней */
    "return the existing task info for a given UUID" in {
      val id = UUID.fromString("6f9ed143-70f4-4406-9c6b-2d9ddd297304")
      val taskInfo = TaskInfo(id, 35, TaskStatus.InProgress)

      mockExpects(id).returning(
        Future.successful(Some(taskInfo))
      )

      Get(s"$baseUrl/$id") ~> route ~> check {
        responseAs[ApiTaskInfo] shouldEqual ApiTaskInfo(id, 35, "InProgress")
      }
    }

    /** TODO Запрос информации о задаче со статусом Queued */

    /** TODO Запрос информации о задаче со статусом Finished */

    /** При запросе информации о несуществующей задаче возвращается 404 Not Found */
    "return 404 when there is no task with the provided id" in {
      val id = UUID.fromString("352bb20e-b593-4934-a60f-9374da5a1f5a")

      mockExpects(id).returning(
        Future.successful(None)
      )

      Get(s"$baseUrl/$id") ~> route ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }

    /** При ошибке в UUID запроса информации о задаче возвращается 404 Not Found */
    "return 404 Not Found when given an invalid UUID" in {
      Get(s"$baseUrl/123") ~> route ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }

    /** При ошибке в сервисе возвращается 500 Internal Server Error */
    "return 500 Internal Server Error when encountering a service error" in {
      val id = UUID.fromString("f01c667d-7bc6-481e-a0e9-de1ced7a2f0d")

      mockExpects(id).returning(
        Future.failed(new RuntimeException)
      )

      Get(s"$baseUrl/$id") ~> route ~> check {
        status shouldEqual StatusCodes.InternalServerError
      }
    }

  }

  /** Запрос состояния разработчика */
  "The service processing a developer state query" should {

    /** Возвращается текущее состояние разработчика */
    "return the current state" in {
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

    /** TODO При ошибке при запросе состояния разработчика возвращается 500 */

  }

}
