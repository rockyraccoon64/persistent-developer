package rr64.developer.infrastructure.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.testkit.ScalatestRouteTest
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

    /** TODO При ошибке при запросе состояния разработчика возвращается 500 */

    /** Запрос информации о существующей задаче */
    "return the task info" in {
      val id = UUID.fromString("6f9ed143-70f4-4406-9c6b-2d9ddd297304")
      val taskInfo = TaskInfo(id, 35, TaskStatus.InProgress)
      (service.taskInfo(_)(_: ExecutionContext))
        .expects(id, *)
        .returning(
          Future.successful(Some(taskInfo))
        )
      Get(s"/api/query/task-info/${id.toString}") ~> route ~> check {
        responseAs[ApiTaskInfo] shouldEqual ApiTaskInfo(id, 35, ApiTaskStatus.InProgress)
      }
    }

    /** TODO При ошибке в UUID запроса информации о задаче возвращается ??? */

  }

}
