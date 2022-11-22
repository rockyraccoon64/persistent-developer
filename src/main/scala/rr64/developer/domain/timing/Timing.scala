package rr64.developer.domain.timing

import rr64.developer.domain.task.Difficulty

import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * Утилиты для расчёта времени
 * */
object Timing {

  /** Расчитать время исполнения задачи или отдыха */
  def calculateTime(difficulty: Difficulty, factor: Factor): FiniteDuration =
    (difficulty.value * factor.value).millis

}
