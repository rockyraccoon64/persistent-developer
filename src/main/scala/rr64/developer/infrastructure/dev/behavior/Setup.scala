package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.scaladsl.TimerScheduler
import rr64.developer.domain.Factor

/**
 * Данные инициализации актора
 * */
private[behavior] case class Setup(
  workFactor: Factor,
  restFactor: Factor,
  timer: TimerScheduler[Command]
)

object Setup {

  def apply(
    workFactor: Int,
    restFactor: Int,
    timer: TimerScheduler[Command]
  ): Setup = Setup(
    workFactor = Factor(workFactor),
    restFactor = Factor(restFactor),
    timer = timer
  )

}