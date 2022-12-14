package rr64.developer.infrastructure.dev.behavior

import akka.actor.testkit.typed.scaladsl.{ManualTime, ScalaTestWithActorTestKit}
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import akka.persistence.typed.PersistenceId
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Inside.inside
import org.scalatest.wordspec.AnyWordSpecLike
import rr64.developer.domain.task.{Difficulty, Task}
import rr64.developer.domain.timing.{Factor, Timing}
import rr64.developer.infrastructure.DeveloperEventTestFacade._
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

  private val testDeveloper = new TestDeveloper(workFactor = 10, restFactor = 5)

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
      val task = Task(15)
      val result = addTask(task)
      val reply = result.replyOfType[Replies.TaskStarted]
      reply.id should not be null
      result.stateOfType[State.Working].currentTask.id shouldEqual reply.id
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
      val task = Task(50)
      val workTime = calculateWorkTime(task.difficulty)
      val restTime = calculateRestTime(task.difficulty)

      addTask(task)

      manualTime.timePasses(workTime)
      manualTime.timePasses(restTime)

      developerTestKit.getState() should not be a [State.Resting]
    }

    /** Когда разработчик работает над задачей,
     * то при получении новой задачи он присваивает ей идентификатор
     * и отправляет его в ответе */
    "reply with an identifier after receiving a new task while working" in {
      val currentTask = TaskWithId(100, "f490d7ca-dcbf-4905-be03-ffd7bf90b513")
      val newTask = Task(10)
      developerTestKit.initialize(taskStartedEvent(currentTask))
      val result = addTask(newTask)
      val reply = result.replyOfType[Replies.TaskQueued]
      reply.id should not be null
    }

    /** Когда разработчик работает над задачей, новые задачи отправляются в очередь */
    "queue new tasks while working" in {
      val firstTask = Task(100)
      val secondTask = Task(50)
      val thirdTask = Task(25)

      val firstResult = addTask(firstTask)
      val secondResult = addTask(secondTask)
      val thirdResult = addTask(thirdTask)

      firstResult.stateOfType[State.Working].taskQueue shouldEqual Nil

      inside(secondResult.stateOfType[State.Working].taskQueue) {
        case Seq(taskWithId) =>
          taskWithId.task shouldEqual secondTask
      }

      inside(thirdResult.stateOfType[State.Working].taskQueue) {
        case Seq(taskWithId1, taskWithId2) =>
          taskWithId1.task shouldEqual secondTask
          taskWithId2.task shouldEqual thirdTask
      }
    }

    /** После окончания работы над задачей очередь задач сохраняется */
    "remain the same when a task is finished" in {
      val firstTask = Task(100)
      val secondTask = Task(50)
      val thirdTask = Task(25)

      addTask(firstTask)

      val secondTaskWithId = queueTask(secondTask)
      val thirdTaskWithId = queueTask(thirdTask)

      val workTime = calculateWorkTime(firstTask.difficulty)

      manualTime.timePasses(workTime)

      inside(developerTestKit.getState()) {
        case State.Resting(_, taskQueue) =>
          taskQueue should contain theSameElementsInOrderAs Seq(secondTaskWithId, thirdTaskWithId)
      }
    }

    /** После отдыха берётся первая задача из очереди, если имеется */
    "take the first task from the queue when he's finished resting" in {
      val firstTask = Task(100)
      val secondTask = Task(90)
      val thirdTask = Task(25)

      addTask(firstTask)

      val secondTaskWithId = queueTask(secondTask)
      val thirdTaskWithId = queueTask(thirdTask)

      val workTime = calculateWorkTime(firstTask.difficulty)
      val restTime = calculateRestTime(firstTask.difficulty)

      manualTime.timePasses(workTime)
      manualTime.timePasses(restTime)

      inside(developerTestKit.getState()) {
        case State.Working(currentTask, taskQueue) =>
          currentTask shouldEqual secondTaskWithId
          taskQueue should contain theSameElementsInOrderAs Seq(thirdTaskWithId)
      }
    }

    /** Если задач в очереди нет, после отдыха разработчик возвращается в свободное состояние */
    "be free after resting if there are no more tasks in the queue" in {
      val task = Task(50)
      val workTime = calculateWorkTime(task.difficulty)
      val restTime = calculateRestTime(task.difficulty)

      addTask(task)

      manualTime.timePasses(workTime)
      manualTime.timePasses(restTime)

      developerTestKit.getState() shouldEqual State.Free
    }

    /** Если разработчик отдыхает, новые задачи ставятся в очередь */
    "queue tasks while resting" in {
      val initialTask = Task(1)
      val workTime = calculateWorkTime(initialTask.difficulty)

      addTask(initialTask)

      manualTime.timePasses(workTime)

      developerTestKit.getState() shouldBe a [State.Resting]

      val taskWithId1 = queueTask(Task(10))
      val taskWithId2 = queueTask(Task(5))

      inside(developerTestKit.getState()) {
        case resting: State.Resting =>
          resting.taskQueue should contain theSameElementsInOrderAs Seq(taskWithId1, taskWithId2)
      }
    }

    /** Если актор упал в рабочем состоянии, соответствующий таймер запускается по новой */
    "start the work timer when completing recovery in a Working state" in {
      val taskWithId = TaskWithId(50, "92ac4c4b-622f-44ba-b331-f1cf40a27c58")
      val workTime = calculateWorkTime(taskWithId.difficulty)

      developerTestKit.initialize(taskStartedEvent(taskWithId))
      developerTestKit.restart()

      manualTime.timePasses(workTime - 1.millis)
      developerTestKit.getState() shouldBe a [State.Working]

      manualTime.timePasses(1.millis)
      developerTestKit.getState() shouldBe a [State.Resting]
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
