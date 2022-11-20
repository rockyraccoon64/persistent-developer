package rr64.developer.domain

case class Task(difficulty: Int) {
  require(difficulty > 0 && difficulty <= 100)
}
