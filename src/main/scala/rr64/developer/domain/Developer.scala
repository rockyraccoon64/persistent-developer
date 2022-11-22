package rr64.developer.domain

import scala.concurrent.{ExecutionContext, Future}

/**
 * Разработчик
 * */
trait Developer {

  /** Текущее состояние */
  def state(implicit ec: ExecutionContext): Future[DeveloperState]
  
  /** Поручить задачу */
  def addTask(task: Task)(implicit ec: ExecutionContext): Future[DeveloperReply]

}
