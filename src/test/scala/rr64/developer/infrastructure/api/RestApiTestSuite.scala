package rr64.developer.infrastructure.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalamock.matchers.MockParameter
import org.scalamock.scalatest.MockFactory
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.domain.dev.{DeveloperReply, DeveloperState}
import rr64.developer.domain.service.DeveloperService
import rr64.developer.domain.task.{Difficulty, Task, TaskInfo, TaskStatus}
import spray.json.DefaultJsonProtocol.immSeqFormat
import spray.json.JsObject

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Тесты REST API сервиса разработки
 * */
class RestApiTestSuite
  extends AnyWordSpec
    with Matchers
    with ScalatestRouteTest
    with MockFactory {

  private type Query = Option[Int]

  private val service = mock[DeveloperService[Query]]
  private val extractQuery = mock[QueryExtractor[Option[String], Query]]
  private val route = new RestApi[Query](service, extractQuery).route

  /** Запрос информации о задаче */
  "The service processing a single task info request" should {

    val baseUrl = "/api/query/task-info"

    def mockExpects(id: MockParameter[UUID]) =
      (service.taskInfo(_: UUID)(_: ExecutionContext))
        .expects(id, *)

    def assertTaskInfoReturned(
      id: UUID,
      difficulty: Int,
      status: TaskStatus,
      apiStatus: String
    ): Assertion = {
      val taskInfo = TaskInfo(id, Difficulty(difficulty), status)

      val taskInfoFound = Future.successful(Some(taskInfo))
      mockExpects(id).returning(taskInfoFound)

      Get(s"$baseUrl/$id") ~> route ~> check {
        responseAs[ApiTaskInfo] shouldEqual ApiTaskInfo(id, difficulty, apiStatus)
        response.status shouldEqual StatusCodes.OK
      }
    }

    /** Когда задача существует, возвращается информация о ней */
    "return the existing task info for a given id" in {
      assertTaskInfoReturned(
        id = UUID.fromString("6f9ed143-70f4-4406-9c6b-2d9ddd297304"),
        difficulty = 35,
        status = TaskStatus.InProgress,
        apiStatus = "InProgress"
      )
      assertTaskInfoReturned(
        id = UUID.fromString("5f4e32f8-fc81-49c4-a05c-efbf5aa0d47d"),
        difficulty = 99,
        status = TaskStatus.Queued,
        apiStatus = "Queued"
      )
      assertTaskInfoReturned(
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
    def assertStateReturned(domain: DeveloperState, api: ApiDeveloperState): Assertion = {
      mockService.returning(Future.successful(domain))
      StateRequest ~> route ~> check {
        responseAs[ApiDeveloperState] shouldEqual api
        status shouldEqual StatusCodes.OK
      }
    }

    /** Состояние "Свободен" */
    "return the Free state" in assertStateReturned(DeveloperState.Free, ApiDeveloperState.Free)

    /** Состояние "Работает" */
    "return the Working state" in assertStateReturned(DeveloperState.Working, ApiDeveloperState.Working)

    /** Состояние "Отдыхает" */
    "return the Resting state" in assertStateReturned(DeveloperState.Resting, ApiDeveloperState.Resting)

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

    def mockExpects(task: MockParameter[Task]) =
      (service.addTask(_: Task)(_: ExecutionContext)).expects(task, *)

    def checkReply(difficulty: Int, domainReply: DeveloperReply, apiReply: ApiReply) {
      val task = Task(difficulty)
      val postEntity = ApiTaskToAdd(difficulty)

      val replyFuture = Future.successful(domainReply)
      mockExpects(task).returning(replyFuture)

      Post(url, postEntity) ~> route ~> check {
        responseAs[ApiReply] shouldEqual apiReply
        status shouldEqual StatusCodes.Created
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

    class DifficultyErrorTest(difficulty: Int) {
      val postEntity = ApiTaskToAdd(difficulty)
      val apiError = ApiError.TaskDifficulty

      Post(url, postEntity) ~> route ~> check {
        responseAs[ApiError] shouldEqual apiError
        status shouldEqual StatusCodes.BadRequest
      }
    }

    /** Если у задачи отрицательная сложность, возвращается 400 Bad Request и сообщение об ошибке */
    "return 400 Bad Request when the task has negative difficulty" in
      new DifficultyErrorTest(-1)

    /** Если у задачи сложность равна нулю, возвращается 400 Bad Request и сообщение об ошибке */
    "return 400 Bad Request when the task has zero difficulty" in
      new DifficultyErrorTest(0)

    /** Если у задачи сложность больше 100, возвращается 400 Bad Request и сообщение об ошибке */
    "return 400 Bad Request when the task has difficulty greater than 100" in
      new DifficultyErrorTest(101)

    /** В случае некорректного формата сущности возвращается 400 Bad Request */
    "return 400 Bad Request when encountering an invalid entity" in {
      val postEntity = JsObject()
      Post(url, postEntity) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }

    /** Если сущность отсутствует, возвращается 400 Bad Request */
    "return 400 Bad Request when the task info is missing" in {
      Post(url) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }

    /** В случае асинхронной ошибки возвращается 500 Internal Server Error */
    "return 500 Internal Server Error when encountering an asynchronous error" in {
      mockExpects(*).returning(Future.failed(new RuntimeException))
      val postEntity = ApiTaskToAdd(99)
      Post(url, postEntity) ~> route ~> check {
        status shouldEqual StatusCodes.InternalServerError
      }
    }

    /** В случае синхронной ошибки возвращается 500 Internal Server Error */
    "return 500 Internal Server Error when encountering a synchronous error" in {
      mockExpects(*).throwing(new RuntimeException)
      val postEntity = ApiTaskToAdd(5)
      Post(url, postEntity) ~> route ~> check {
        status shouldEqual StatusCodes.InternalServerError
      }
    }

  }

  /** Во время обработки запроса списка задач API должен */
  "The service processing the task list request" should {

    def sendRequest(query: Option[String] = None): HttpRequest = {
      val queryString = query.map("?query=" + _).getOrElse("")
      Get(s"/api/query/task-list$queryString")
    }

    def mockExtractor(
      expects: MockParameter[Option[String]] = *,
      returns: Either[String, Query] = Right(None)
    ) =
      (extractQuery.extract _)
        .expects(expects)
        .returning(returns)

    def mockService(
      expectedQuery: MockParameter[Query] = *,
      extractorExpects: MockParameter[Option[String]] = *,
      extractorReturns: Either[String, Query] = Right(None)
    ) = {
      mockExtractor(extractorExpects, extractorReturns)
      (service.tasks(_: Query)(_: ExecutionContext)).expects(expectedQuery, *)
    }

    /** Передавать содержимое запроса сервису после парсинга */
    "extract the query" in {
      val input = Some("505")
      val extracted = Some(505)
      mockService(
        expectedQuery = extracted,
        extractorExpects = input,
        extractorReturns = Right(extracted)
      )
      sendRequest(query = input) ~> route
    }

    /** Сообщать об ошибке в запросе */
    "notify when there's an error in the query" in {
      val errorMessage = "The query content should be an integer"
      mockExtractor(returns = Left(errorMessage))
      sendRequest(query = Some("ABC")) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[ApiError] shouldEqual ApiError.inQuery(errorMessage)
      }
    }

    /** Возвращать список задач */
    "return the task list" in {
      val domainTasks = TaskInfo(UUID.randomUUID(), Difficulty(99), TaskStatus.Finished) ::
        TaskInfo(UUID.randomUUID(), Difficulty(51), TaskStatus.InProgress) ::
        TaskInfo(UUID.randomUUID(), Difficulty(65), TaskStatus.Queued) ::
        Nil

      val apiTasks = domainTasks.map(ApiTaskInfo.adapter.convert)

      mockService().returning(Future.successful(domainTasks))

      sendRequest() ~> route ~> check {
        responseAs[Seq[ApiTaskInfo]] should contain theSameElementsInOrderAs apiTasks
        status shouldEqual StatusCodes.OK
      }
    }

    /** Возвращать пустой массив, если задач нет */
    "return an empty list when there are no tasks" in {
      mockService().returning(Future.successful(Nil))

      sendRequest() ~> route ~> check {
        responseAs[Seq[ApiTaskInfo]] should have size 0
      }
    }

    /** Возвращать 500 Internal Server Error в случае асинхронной ошибки */
    "return 500 Internal Server Error when encountering an asynchronous error" in {
      mockService().returning(Future.failed(new RuntimeException))

      sendRequest() ~> route ~> check {
        status shouldEqual StatusCodes.InternalServerError
      }
    }

    /** Возвращать 500 Internal Server Error в случае синхронной ошибки */
    "return 500 Internal Server Error when encountering a synchronous error" in {
      mockService().throwing(new RuntimeException)

      sendRequest() ~> route ~> check {
        status shouldEqual StatusCodes.InternalServerError
      }
    }

  }

}
