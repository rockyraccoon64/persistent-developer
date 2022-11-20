package rr64.developer.domain

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object TaskTiming {

  /** Расчитать время исполнения задачи или отдыха */
  def calculateTime(difficulty: Difficulty, factor: Factor): FiniteDuration =
    (difficulty.value * factor.value).millis

}
