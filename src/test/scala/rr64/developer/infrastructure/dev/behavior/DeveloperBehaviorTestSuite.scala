package rr64.developer.infrastructure.dev.behavior

import akka.actor.testkit.typed.scaladsl.{ManualTime, ScalaTestWithActorTestKit}
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import akka.persistence.typed.PersistenceId
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike
import rr64.developer.domain.task.{Difficulty, Task}
import rr64.developer.domain.timing.{Factor, Timing}
import rr64.developer.infrastructure.DeveloperEventTestFacade._
import rr64.developer.infrastructure.dev.behavior.facade.{DeveloperTestFacade, TestTask}
import rr64.developer.infrastructure.task.TaskTestFacade.createTaskWithId
import rr64.developer.infrastructure.task.TaskWithId

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

  private type Kit = EventSourcedBehaviorTestKit[Command, Event, State]

  private val manualTime = ManualTime()

  private val workFactor = Factor(10)
  private val restFactor = Factor(5)
  private val developerTestKit: Kit =
    EventSourcedBehaviorTestKit(
      system = system,
      behavior = DeveloperBehavior(
        persistenceId = PersistenceId.ofUniqueId("dev-test"),
        workFactor = workFactor,
        restFactor = restFactor
      ),
      SerializationSettings.disabled
    )

  private val testDeveloper = new DeveloperTestFacade(workFactor = 10, restFactor = 5)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    developerTestKit.clear()
    testDeveloper.reset()
  }

  /** Поручить задачу */
  private def addTask(task: Task) =
    developerTestKit.runCommand(Command.AddTask(task, _))

  /** Поставить задачу в очередь и получить идентификатор */
  private def queueTask(task: Task): TaskWithId = {
    val result = addTask(task)
    val id = result.replyOfType[Replies.TaskQueued].id
    TaskWithId(task, id)
  }

  /** Расчитать время работы */
  private def calculateWorkTime(difficulty: Difficulty): FiniteDuration =
    Timing.calculateTime(difficulty, workFactor)

  /** Расчитать время работы */
  private def calculateWorkTime(task: TestTask): FiniteDuration =
    calculateWorkTime(Difficulty(task.difficulty))

  /** Расчитать время отдыха */
  private def calculateRestTime(difficulty: Difficulty): FiniteDuration =
    Timing.calculateTime(difficulty, restFactor)

  /** Расчитать время отдыха */
  private def calculateRestTime(task: TestTask): FiniteDuration =
    calculateRestTime(Difficulty(task.difficulty))

  /** Актор разработчика */
  "The developer" should {

    /** Начинает в свободном состоянии */
    "start in a free state" in testDeveloper.shouldBeFree

    /** Когда разработчик свободен, он принимает задачу в работу */
    "accept the task he's given when he's free" in {
      val task = TestTask(5)
      testDeveloper.addTask(task)
      testDeveloper.shouldBeWorkingOnTask(task)
    }

    /** Когда разработчик свободен, то при получении задачи
     * он присваивает ей идентификатор и отправляет его в ответе */
    "reply with a Task Started message when he's free" in {
      val task = TestTask(15)
      val result = testDeveloper.addTask(task)
      result.taskShouldBeStarted
      result.taskShouldHaveIdentifier
      val id = result.id
      testDeveloper.workingOnTaskWithReturnedIdentifier(id)
    }

    /** До выполнения задачи разработчик работает */
    "work until the task is done" in {
      val task = TestTask(20)
      val workTime = calculateWorkTime(task)

      testDeveloper.addTask(task)

      manualTime.timePasses(workTime - 1.millis)
      testDeveloper.shouldBeWorkingOnTask(task)
      manualTime.timePasses(1.millis)
      testDeveloper.shouldNotBeWorking
    }

    /** Завершив задачу, разработчик делает перерыв */
    "rest after completing a task" in {
      val task = TestTask(50)
      val workTime = calculateWorkTime(task)

      testDeveloper.addTask(task)

      manualTime.timePasses(workTime)
      testDeveloper.shouldRestAfterCompletingTask(task)
    }

    /** Перерыв длится строго отведённое время */
    "only rest for a designated time period" in {
      val task = TestTask(50)
      val workTime = calculateWorkTime(task)
      val restTime = calculateRestTime(task)

      testDeveloper.addTask(task)

      manualTime.timePasses(workTime)
      manualTime.timePasses(restTime)

      testDeveloper.shouldNotBeResting
    }

    /** Когда разработчик работает над задачей,
     * то при получении новой задачи он присваивает ей идентификатор
     * и отправляет его в ответе */
    "reply with an identifier after receiving a new task while working" in {
      val currentTask = createTaskWithId(100, "f490d7ca-dcbf-4905-be03-ffd7bf90b513")
      val newTask = TestTask(10)
      testDeveloper.afterStartingTask(currentTask)
      val result = testDeveloper.addTask(newTask)
      result.taskShouldHaveIdentifier
    }

    /** Когда разработчик работает над задачей, новые задачи отправляются в очередь */
    "queue new tasks while working" in {
      val firstTask = TestTask(100)
      val secondTask = TestTask(50)
      val thirdTask = TestTask(25)

      testDeveloper.addTask(firstTask)
      testDeveloper.queueShouldEqual(Nil)

      testDeveloper.addTask(secondTask)
      testDeveloper.queueShouldEqual(secondTask :: Nil)

      testDeveloper.addTask(thirdTask)
      testDeveloper.queueShouldEqual(secondTask :: thirdTask :: Nil)
    }

    /** После окончания работы над задачей очередь задач сохраняется */
    "leave the queue the same after a task is finished" in {
      val firstTask = TestTask(100)
      val secondTask = TestTask(50)
      val thirdTask = TestTask(25)

      testDeveloper.addTask(firstTask)
      testDeveloper.addTask(secondTask)
      testDeveloper.addTask(thirdTask)

      val workTime = calculateWorkTime(firstTask)
      manualTime.timePasses(workTime)

      testDeveloper.shouldBeResting
      testDeveloper.queueShouldEqual(secondTask :: thirdTask :: Nil)
    }

    /** После отдыха берётся первая задача из очереди, если имеется */
    "take the first task from the queue when he's finished resting" in {
      val firstTask = TestTask(100)
      val secondTask = TestTask(90)
      val thirdTask = TestTask(25)

      testDeveloper.addTask(firstTask)
      testDeveloper.addTask(secondTask)
      testDeveloper.addTask(thirdTask)

      val workTime = calculateWorkTime(firstTask)
      val restTime = calculateRestTime(firstTask)

      manualTime.timePasses(workTime)
      manualTime.timePasses(restTime)

      testDeveloper.shouldBeWorkingOnTask(secondTask)
      testDeveloper.queueShouldEqual(thirdTask :: Nil)
    }

    /** Если задач в очереди нет, после отдыха разработчик возвращается в свободное состояние */
    "be free after resting if there are no more tasks in the queue" in {
      val task = TestTask(50)
      val workTime = calculateWorkTime(task)
      val restTime = calculateRestTime(task)

      testDeveloper.addTask(task)

      manualTime.timePasses(workTime)
      manualTime.timePasses(restTime)

      testDeveloper.shouldBeFree
    }

    /** Если разработчик отдыхает, новые задачи ставятся в очередь */
    "queue tasks while resting" in {
      val initialTask = TestTask(1)
      val workTime = calculateWorkTime(initialTask)

      testDeveloper.addTask(initialTask)
      manualTime.timePasses(workTime)

      testDeveloper.shouldBeResting

      val secondTask = TestTask(10)
      val thirdTask = TestTask(5)

      testDeveloper.addTask(secondTask)
      testDeveloper.addTask(thirdTask)

      testDeveloper.queueShouldEqual(secondTask :: thirdTask :: Nil)
    }

    /** Если актор упал в рабочем состоянии, соответствующий таймер запускается по новой */
    "start the work timer when completing recovery in a Working state" in {
      val task = TestTask(50)
      val taskWithId = createTaskWithId(50, "92ac4c4b-622f-44ba-b331-f1cf40a27c58")
      val workTime = calculateWorkTime(taskWithId.difficulty)

      testDeveloper.afterStartingTask(taskWithId)
      testDeveloper.fail()

      manualTime.timePasses(workTime - 1.millis)
      testDeveloper.shouldBeWorkingOnTask(task)

      manualTime.timePasses(1.millis)
      testDeveloper.shouldBeResting
    }

    /** Если актор упал в состоянии отдыха, соответствующий таймер запускается по новой */
    "start the rest timer when completing recovery in a Resting state" in {
      val taskWithId = TaskWithId(10, "b807f5ff-6066-454e-8d53-2a90a3941cc4")
      val restTime = calculateRestTime(taskWithId.difficulty)

      developerTestKit.initialize(taskStartedEvent(taskWithId), taskFinishedEvent(taskWithId))
      developerTestKit.restart()

      manualTime.timePasses(restTime - 1.millis)
      developerTestKit.getState() shouldBe a [State.Resting]

      manualTime.timePasses(1.millis)
      developerTestKit.getState() shouldBe State.Free
    }

    /** После отдыха разработчик выполняет следующую задачу из очереди до конца */
    "fully complete the next task in the queue after resting" in {
      val lastCompleted = TaskWithId(12, "6bf0af94-4ee3-4857-9a38-3e31e529b37d")
      val taskQueue = TaskWithId(35, "ba5be578-9af1-44a6-9b8b-0a11c340237b") ::
        TaskWithId(19, "da2b386f-a53e-44a8-b943-8e7491d1010e") ::
        Nil
      val restTime = calculateRestTime(lastCompleted.difficulty)
      val nextTask = taskQueue.head
      val workTime = calculateWorkTime(nextTask.difficulty)

      developerTestKit.initialize(State.Resting(lastCompleted, taskQueue))

      manualTime.timePasses(restTime)
      developerTestKit.getState() shouldEqual State.Working(nextTask, taskQueue.tail)
      manualTime.timePasses(workTime - 1.millis)
      developerTestKit.getState() shouldEqual State.Working(nextTask, taskQueue.tail)
      manualTime.timePasses(1.millis)
      developerTestKit.getState() shouldEqual State.Resting(nextTask, taskQueue.tail)
    }

  }

}
