package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.scaladsl.TimerScheduler

/**
 * Данные инициализации актора
 * */
private[behavior] case class Setup(
  workFactor: Int,
  restFactor: Int,
  timer: TimerScheduler[Command]
)
