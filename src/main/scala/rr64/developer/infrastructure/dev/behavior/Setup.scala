package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.scaladsl.{ActorContext, TimerScheduler}
import rr64.developer.domain.timing.Factor

/**
 * Данные инициализации актора
 * @param workFactor Рабочий множитель
 * @param restFactor Множитель отдыха
 * @param timer Таймер работы/отдыха
 * */
private[behavior] case class Setup(
  workFactor: Factor,
  restFactor: Factor,
  timer: TimerScheduler[Command],
  context: ActorContext[Command]
)