package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.scaladsl.TimerScheduler
import rr64.developer.domain.Factor
import rr64.developer.infrastructure.task.TaskWithId

import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * Вспомогательные методы для таймеров
 * актора разработчика
 * */
object Timing {

  /** Расчитать время таймера, исходя из сложности задачи и множителя */
  def calculateTime(difficulty: Int, factor: Factor): FiniteDuration =
    (difficulty * factor.value).millis

  /** Запустить таймер до завершения задачи */
  private[behavior] def startWorkTimer(taskWithId: TaskWithId)
      (implicit setup: Setup): Unit =
    startTimer(
      timer = setup.timer,
      message = Command.FinishTask(taskWithId.id),
      difficulty = taskWithId.difficulty,
      factor = setup.workFactor
    )

  /** Запустить таймер до завершения отдыха */
  private[behavior] def startRestTimer(taskWithId: TaskWithId)
      (implicit setup: Setup): Unit =
    startTimer(
      timer = setup.timer,
      message = Command.StopResting,
      difficulty = taskWithId.difficulty,
      factor = setup.restFactor
    )

  /** Запустить таймер, исходя из сложности задачи и множителя */
  private def startTimer(
    timer: TimerScheduler[Command],
    message: Command,
    difficulty: Int,
    factor: Factor
  ): Unit = {
    val delay = calculateTime(difficulty, factor)
    timer.startSingleTimer(message, delay)
  }

}
