package rr64.developer.infrastructure.dev.behavior

import akka.actor.testkit.typed.scaladsl.{ManualTime, ScalaTestWithActorTestKit}
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike
import rr64.developer.infrastructure.facade.dev.DeveloperTestFacade
import rr64.developer.infrastructure.facade.task.{TestTask, TestTaskWithId}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * Тесты поведения персистентного актора разработчика
 */
class DeveloperBehaviorTestSuite
  extends ScalaTestWithActorTestKit(
    ManualTime.config
      .withFallback(EventSourcedBehaviorTestKit.config)
  ) with AnyWordSpecLike
    with BeforeAndAfterEach {

  private val manualTime = ManualTime()
  private val developer = DeveloperTestFacade(
    workFactor = 10,
    restFactor = 5
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    developer.reset()
  }

  /** Расчитать время работы */
  private def calculateWorkTime(task: TestTask): FiniteDuration =
    developer.calculateWorkTime(task)

  /** Расчитать время отдыха */
  private def calculateRestTime(task: TestTask): FiniteDuration =
    developer.calculateRestTime(task)

  /** Актор разработчика */
  "The developer" should {

    /** Начинает в свободном состоянии */
    "start in a free state" in developer.shouldBeFree

    /** Когда разработчик свободен, он принимает задачу в работу */
    "accept the task he's given when he's free" in {
      val task = TestTask(5)
      developer.addTask(task)
      developer.shouldBeWorkingOnTask(task)
    }

    /** Когда разработчик свободен, то при получении задачи
     * он присваивает ей идентификатор и отправляет его в ответе */
    "reply with a Task Started message when he's free" in {
      val task = TestTask(15)
      val result = developer.addTask(task)
      result.taskShouldBeStarted
      result.taskShouldHaveIdentifier
      val id = result.taskId
      developer.shouldBeWorkingOnTaskWithId(id)
    }

    /** До выполнения задачи разработчик работает */
    "work until the task is done" in {
      val task = TestTask(20)
      val workTime = calculateWorkTime(task)

      developer.addTask(task)

      manualTime.timePasses(workTime - 1.millis)
      developer.shouldBeWorkingOnTask(task)
      manualTime.timePasses(1.millis)
      developer.shouldNotBeWorking
    }

    /** Завершив задачу, разработчик делает перерыв */
    "rest after completing a task" in {
      val task = TestTask(50)
      val workTime = calculateWorkTime(task)

      developer.addTask(task)

      manualTime.timePasses(workTime)
      developer.shouldBeRestingAfterCompletingTask(task)
    }

    /** Перерыв длится строго отведённое время */
    "only rest for a designated time period" in {
      val task = TestTask(50)
      val workTime = calculateWorkTime(task)
      val restTime = calculateRestTime(task)

      developer.addTask(task)

      manualTime.timePasses(workTime)
      manualTime.timePasses(restTime)

      developer.shouldNotBeResting
    }

    /** Когда разработчик работает над задачей,
     * то при получении новой задачи он присваивает ей идентификатор
     * и отправляет его в ответе */
    "reply with an identifier after receiving a new task while working" in {
      val currentTask = TestTaskWithId(100, "f490d7ca-dcbf-4905-be03-ffd7bf90b513")
      val newTask = TestTask(10)
      developer.afterStartingTask(currentTask)
      val result = developer.addTask(newTask)
      result.taskShouldHaveIdentifier
    }

    /** Когда разработчик работает над задачей, новые задачи отправляются в очередь */
    "queue new tasks while working" in {
      val firstTask = TestTask(100)
      val secondTask = TestTask(50)
      val thirdTask = TestTask(25)

      developer.addTask(firstTask)
      developer.queueShouldEqual(Nil)

      developer.addTask(secondTask)
      developer.queueShouldEqual(secondTask :: Nil)

      developer.addTask(thirdTask)
      developer.queueShouldEqual(secondTask :: thirdTask :: Nil)
    }

    /** После окончания работы над задачей очередь задач сохраняется */
    "leave the queue the same after a task is finished" in {
      val firstTask = TestTask(100)
      val secondTask = TestTask(50)
      val thirdTask = TestTask(25)

      developer.addTask(firstTask)
      developer.addTask(secondTask)
      developer.addTask(thirdTask)

      val workTime = calculateWorkTime(firstTask)
      manualTime.timePasses(workTime)

      developer.shouldBeResting
      developer.queueShouldEqual(secondTask :: thirdTask :: Nil)
    }

    /** После отдыха берётся первая задача из очереди, если имеется */
    "take the first task from the queue when he's finished resting" in {
      val firstTask = TestTask(100)
      val secondTask = TestTask(90)
      val thirdTask = TestTask(25)

      developer.addTask(firstTask)
      developer.addTask(secondTask)
      developer.addTask(thirdTask)

      val workTime = calculateWorkTime(firstTask)
      val restTime = calculateRestTime(firstTask)

      manualTime.timePasses(workTime)
      manualTime.timePasses(restTime)

      developer.shouldBeWorkingOnTask(secondTask)
      developer.queueShouldEqual(thirdTask :: Nil)
    }

    /** Если задач в очереди нет, после отдыха разработчик возвращается в свободное состояние */
    "be free after resting if there are no more tasks in the queue" in {
      val task = TestTask(50)
      val workTime = calculateWorkTime(task)
      val restTime = calculateRestTime(task)

      developer.addTask(task)

      manualTime.timePasses(workTime)
      manualTime.timePasses(restTime)

      developer.shouldBeFree
    }

    /** Если разработчик отдыхает, новые задачи ставятся в очередь */
    "queue tasks while resting" in {
      val initialTask = TestTask(1)
      val workTime = calculateWorkTime(initialTask)

      developer.addTask(initialTask)
      manualTime.timePasses(workTime)

      developer.shouldBeResting

      val secondTask = TestTask(10)
      val thirdTask = TestTask(5)

      developer.addTask(secondTask)
      developer.addTask(thirdTask)

      developer.queueShouldEqual(secondTask :: thirdTask :: Nil)
    }

    /** Если актор упал в рабочем состоянии, соответствующий таймер запускается по новой */
    "start the work timer when completing recovery in a Working state" in {
      val taskWithId = TestTaskWithId(50, "92ac4c4b-622f-44ba-b331-f1cf40a27c58")
      val task = taskWithId.toTask
      val workTime = calculateWorkTime(task)

      developer.afterStartingTask(taskWithId)
      developer.restart()

      manualTime.timePasses(workTime - 1.millis)
      developer.shouldBeWorkingOnTask(task)

      manualTime.timePasses(1.millis)
      developer.shouldBeResting
    }

    /** Если актор упал в состоянии отдыха, соответствующий таймер запускается по новой */
    "start the rest timer when completing recovery in a Resting state" in {
      val taskWithId = TestTaskWithId(10, "b807f5ff-6066-454e-8d53-2a90a3941cc4")
      val restTime = calculateRestTime(taskWithId.toTask)

      developer.afterCompletingTask(taskWithId)
      developer.restart()

      manualTime.timePasses(restTime - 1.millis)
      developer.shouldBeResting

      manualTime.timePasses(1.millis)
      developer.shouldBeFree
    }

    /** После отдыха разработчик выполняет следующую задачу из очереди до конца */
    "fully complete the next task in the queue after resting" in {
      val lastCompleted = TestTaskWithId(12, "6bf0af94-4ee3-4857-9a38-3e31e529b37d")
      val taskQueue = TestTaskWithId(35, "ba5be578-9af1-44a6-9b8b-0a11c340237b") ::
        TestTaskWithId(19, "da2b386f-a53e-44a8-b943-8e7491d1010e") ::
        Nil
      val restTime = calculateRestTime(lastCompleted.toTask)
      val nextTask = taskQueue.head.toTask
      val nextQueue = taskQueue.tail.map(_.toTask)
      val workTime = calculateWorkTime(nextTask)

      developer.whileResting(lastCompleted, taskQueue)

      manualTime.timePasses(restTime)
      developer.shouldBeWorkingOnTask(nextTask)
      developer.queueShouldEqual(nextQueue)

      manualTime.timePasses(workTime - 1.millis)
      developer.shouldBeWorkingOnTask(nextTask)
      developer.queueShouldEqual(nextQueue)

      manualTime.timePasses(1.millis)
      developer.shouldBeRestingAfterCompletingTask(nextTask)
      developer.queueShouldEqual(nextQueue)
    }

  }

}
