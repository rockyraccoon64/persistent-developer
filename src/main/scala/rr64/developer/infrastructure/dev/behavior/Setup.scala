package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.scaladsl.TimerScheduler

/**
 * Данные инициализации актора
 * */
private[behavior] case class Setup private(
  workFactor: Int,
  restFactor: Int,
  timer: TimerScheduler[Command]
)

object Setup {

  def apply(
    workFactor: Int,
    restFactor: Int,
    timer: TimerScheduler[Command]
  ): Setup = {
    if (workFactor > 0 && workFactor <= 1000 && restFactor > 0)
      new Setup(workFactor, restFactor, timer)
    else
      throw new FactorException
  }

  class FactorException extends RuntimeException

}