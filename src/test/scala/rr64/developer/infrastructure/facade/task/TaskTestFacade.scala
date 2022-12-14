package rr64.developer.infrastructure.facade.task

/**
 * Фасад для тестов с использованием задач
 * */
trait TaskTestFacade
  extends TaskStatusTestFacade
    with TaskWithIdTestFacade
    with TaskInfoTestFacade
    with TaskRepositoryTestFacade

object TaskTestFacade
  extends TaskTestFacade