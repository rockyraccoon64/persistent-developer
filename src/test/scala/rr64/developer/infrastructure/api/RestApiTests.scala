package rr64.developer.infrastructure.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalamock.matchers.MockParameter
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.domain._
import spray.json.JsObject

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

    def checkTask(id: UUID, difficulty: Int, status: TaskStatus, apiStatus: String) = {
      val taskInfo = TaskInfo(id, difficulty, status)

      val taskInfoFound = Future.successful(Some(taskInfo))
      mockExpects(id).returning(taskInfoFound)

      Get(s"$baseUrl/$id") ~> route ~> check {
        responseAs[ApiTaskInfo] shouldEqual ApiTaskInfo(id, difficulty, apiStatus)
        response.status shouldEqual StatusCodes.OK
      }
    }

    /** Когда задача существует, возвращается информация о ней */
    "return the existing task info for a given id" in {
      checkTask(
        id = UUID.fromString("6f9ed143-70f4-4406-9c6b-2d9ddd297304"),
        difficulty = 35,
        status = TaskStatus.InProgress,
        apiStatus = "InProgress"
      )
      checkTask(
        id = UUID.fromString("5f4e32f8-fc81-49c4-a05c-efbf5aa0d47d"),
        difficulty = 99,
        status = TaskStatus.Queued,
        apiStatus = "Queued"
      )
      checkTask(
        id = UUID.fromString("374b7d13-8174-4476-b1d6-1d8759d2a6ed"),
        difficulty = 1,
        status = TaskStatus.Finished,
        apiStatus = "Finished"
      )
    }

    /** При запросе информации о несуществующей задаче возвращается 404 Not Found */
    "return 404 when there is no task with the provided id" in {
      val id = UUID.fromString("352bb20e-b593-4934-a60f-9374da5a1f5a")

      val taskNotFound = Future.successful(None)
      mockExpects(id).returning(taskNotFound)

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

    /** При асинхронной ошибке в сервисе возвращается 500 Internal Server Error */
    "return 500 Internal Server Error when encountering an asynchronous exception" in {
      val id = UUID.fromString("f01c667d-7bc6-481e-a0e9-de1ced7a2f0d")

      val failure = Future.failed(new RuntimeException)
      mockExpects(id).returning(failure)

      Get(s"$baseUrl/$id") ~> route ~> check {
        status shouldEqual StatusCodes.InternalServerError
      }
    }

    /** При синхронном исключении возвращается 500 Internal Server Error */
    "return 500 Internal Server Error when encountering a synchronous exception" in {
      val id = UUID.fromString("4bd97557-b3a1-4404-84c9-bc6a9e96723c")
      mockExpects(id).throwing(new RuntimeException)
      Get(s"$baseUrl/$id") ~> route ~> check {
        status shouldEqual StatusCodes.InternalServerError
      }
    }

  }

  /** Запрос состояния разработчика */
  "The service processing a developer state query" should {

    val StateRequest = Get("/api/query/developer-state")

    def mockService =
      (service.developerState(_: ExecutionContext)).expects(*)

    /** Проверка текущего состояния */
    class StateTest(domain: DeveloperState, api: ApiDeveloperState) {
      mockService.returning(Future.successful(domain))
      StateRequest ~> route ~> check {
        responseAs[ApiDeveloperState] shouldEqual api
        status shouldEqual StatusCodes.OK
      }
    }

    /** Состояние "Свободен" */
    "return the Free state" in new StateTest(DeveloperState.Free, ApiDeveloperState.Free)

    /** Состояние "Работает" */
    "return the Working state" in new StateTest(DeveloperState.Working, ApiDeveloperState.Working)

    /** Состояние "Отдыхает" */
    "return the Resting state" in new StateTest(DeveloperState.Resting, ApiDeveloperState.Resting)

    /** При асинхронной ошибке возвращается 500 Internal Server Error */
    "return 500 Internal Server Error when encountering an asynchronous exception" in {
      mockService.returning(Future.failed(new RuntimeException))
      StateRequest ~> route ~> check {
        status shouldEqual StatusCodes.InternalServerError
      }
    }

    /** При синхронной ошибке возвращается 500 Internal Server Error */
    "return 500 Internal Server Error when encountering a synchronous exception" in {
      mockService.throwing(new RuntimeException)
      StateRequest ~> route ~> check {
        status shouldEqual StatusCodes.InternalServerError
      }
    }

  }

  /** Команда поручения задачи */
  "The service processing the Add Task command" should {

    val url = "/api/command/add-task"

    def mockExpect(task: MockParameter[Task]) =
      (service.addTask(_: Task)(_: ExecutionContext)).expects(task, *)

    def checkReply(difficulty: Int, domainReply: DeveloperReply, apiReply: ApiReply) {
      val task = Task(difficulty)
      val postEntity = ApiTaskToAdd(difficulty)

      val replyFuture = Future.successful(domainReply)
      mockExpect(task).returning(replyFuture)

      Post(url, postEntity) ~> route ~> check {
        responseAs[ApiReply] shouldEqual apiReply
        status shouldEqual StatusCodes.OK
      }
    }

    /** Когда задача начата, возвращается её идентификатор и соответствующий признак */
    "return the Task Started reply" in {
      val difficulty = 9
      val id = UUID.fromString("f03fb7d3-2e2b-4965-b85c-f91692a583ff")
      val domainReply = DeveloperReply.TaskStarted(id)
      val apiReply = ApiReply(id, "Started")

      checkReply(difficulty, domainReply, apiReply)
    }

    /** Когда задача поставлена в очередь, возвращается её идентификатор и соответствующий признак */
    "return the Task Queued reply" in {
      val difficulty = 10
      val id = UUID.fromString("f89474c0-8c5a-4f3f-8e8c-92c483c30bd1")
      val domainReply = DeveloperReply.TaskQueued(id)
      val apiReply = ApiReply(id, "Queued")

      checkReply(difficulty, domainReply, apiReply)
    }

    /** В случае некорректного формата сущности возвращается 400 Bad Request */
    "return 400 Bad Request when encountering an invalid entity" in {
      val postEntity = JsObject()
      Post(url, postEntity) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }

    /** TODO Сущность отсутствует */

    /** В случае асинхронной ошибки возвращается 500 Internal Server Error */
    "return 500 Internal Server Error when encountering an asynchronous error" in {
      mockExpect(*).returning(Future.failed(new RuntimeException))
      val postEntity = ApiTaskToAdd(99)
      Post(url, postEntity) ~> route ~> check {
        status shouldEqual StatusCodes.InternalServerError
      }
    }

    /** В случае синхронной ошибки возвращается 500 Internal Server Error */
    "return 500 Internal Server Error when encountering a synchronous error" in {
      mockExpect(*).throwing(new RuntimeException)
      val postEntity = ApiTaskToAdd(5)
      Post(url, postEntity) ~> route ~> check {
        status shouldEqual StatusCodes.InternalServerError
      }
    }

  }

}
