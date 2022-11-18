package rr64.developer.infrastructure.api

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class RestApiTests
  extends AnyFlatSpec
    with Matchers
    with ScalatestRouteTest {

  private val service = new DeveloperService {
    override def addTask(task: Task)(implicit ec: ExecutionContext): Future[DeveloperReply] = ???
    override def developerState(implicit ec: ExecutionContext): Future[DeveloperState] = ???
    override def taskInfo(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]] = ???
    override def tasks(implicit ec: ExecutionContext): Future[Seq[TaskInfo]] = ???
  }

  private val route = new RestApi(service).route

}
