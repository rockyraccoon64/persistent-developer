package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.scaladsl.TimerScheduler
import rr64.developer.domain.{Difficulty, Factor, TaskTiming}
import rr64.developer.infrastructure.task.TaskWithId

/**
 * Вспомогательные методы для таймеров
 * актора разработчика
 * */
object Timing {

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
    difficulty: Difficulty,
    factor: Factor
  ): Unit = {
    val delay = TaskTiming.calculateTime(difficulty, factor)
    timer.startSingleTimer(message, delay)
  }

}
