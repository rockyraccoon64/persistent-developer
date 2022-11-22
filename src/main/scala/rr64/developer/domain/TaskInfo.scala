package rr64.developer.domain

import java.util.UUID

/**
 * Информация о существующей задаче
 * @param id Идентификатор
 * @param difficulty Сложность
 * @param status Текущий статус
 * */
case class TaskInfo(id: UUID, difficulty: Difficulty, status: TaskStatus)
