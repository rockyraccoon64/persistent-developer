package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.scaladsl.TimerScheduler
import rr64.developer.domain.task.Difficulty
import rr64.developer.domain.timing.{Factor, Timing}
import rr64.developer.infrastructure.task.TaskWithId

/**
 * Вспомогательные методы для таймеров
 * актора разработчика
 * */
object Timers {

  /**
   * Запустить таймер до завершения задачи
   * @param taskWithId Задача
   * */
  private[behavior] def startWorkTimer(taskWithId: TaskWithId)
      (implicit setup: Setup): Unit =
    startTimer(
      timer = setup.timer,
      command = Command.FinishTask(taskWithId.id),
      difficulty = taskWithId.difficulty,
      factor = setup.workFactor
    )

  /**
   * Запустить таймер до завершения отдыха
   * @param taskWithId Последняя завершённая задача
   * */
  private[behavior] def startRestTimer(taskWithId: TaskWithId)
      (implicit setup: Setup): Unit =
    startTimer(
      timer = setup.timer,
      command = Command.StopResting,
      difficulty = taskWithId.difficulty,
      factor = setup.restFactor
    )

  /**
   * Запустить таймер, исходя из сложности задачи и множителя
   * @param timer Таймер актора
   * @param command Команда актору
   * @param difficulty Сложность задачи
   * @param factor Множитель
   * */
  private def startTimer(
    timer: TimerScheduler[Command],
    command: Command,
    difficulty: Difficulty,
    factor: Factor
  ): Unit = {
    val delay = Timing.calculateTime(difficulty, factor)
    timer.startSingleTimer(command, delay)
  }

}
