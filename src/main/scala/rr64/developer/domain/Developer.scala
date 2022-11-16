package rr64.developer.domain

import scala.concurrent.{ExecutionContext, Future}

trait Developer {
  def state(implicit ec: ExecutionContext): Future[DeveloperState]
  def addTask(task: Task)(implicit ec: ExecutionContext): Future[DeveloperReply]
}
