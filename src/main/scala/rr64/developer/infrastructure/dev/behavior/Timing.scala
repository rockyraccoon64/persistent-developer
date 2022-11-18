package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.scaladsl.TimerScheduler
import rr64.developer.infrastructure.task.TaskWithId

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Timing {

  def calculateTime(difficulty: Int, factor: Int): FiniteDuration =
    (difficulty * factor).millis

  private[behavior] def startWorkTimer(taskWithId: TaskWithId)
      (implicit setup: Setup): Unit =
    startTimer(
      timer = setup.timer,
      message = Command.FinishTask(taskWithId.id),
      difficulty = taskWithId.task.difficulty,
      factor = setup.workFactor
    )

  private[behavior] def startRestTimer(taskWithId: TaskWithId)
      (implicit setup: Setup): Unit =
    startTimer(
      timer = setup.timer,
      message = Command.StopResting,
      difficulty = taskWithId.task.difficulty,
      factor = setup.restFactor
    )

  private def startTimer(
    timer: TimerScheduler[Command],
    message: Command,
    difficulty: Int,
    factor: Int
  ): Unit = {
    val delay = calculateTime(difficulty, factor)
    timer.startSingleTimer(message, delay)
  }

}
