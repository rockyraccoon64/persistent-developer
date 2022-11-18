package rr64.developer.infrastructure.dev

import akka.actor.testkit.typed.scaladsl.{ManualTime, ScalaTestWithActorTestKit}
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import akka.persistence.typed.PersistenceId
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpecLike
import rr64.developer.domain.Task
import rr64.developer.infrastructure.TaskTestUtils.TaskWithIdFactory
import rr64.developer.infrastructure.dev.DeveloperBehavior.State.Working
import rr64.developer.infrastructure.dev.DeveloperBehavior._
import rr64.developer.infrastructure.dev.behavior.Replies
import rr64.developer.infrastructure.task.TaskWithId

import java.util.UUID
import scala.concurrent.duration.{DurationInt, FiniteDuration}

class DeveloperBehaviorTestSuite
  extends ScalaTestWithActorTestKit(
    ManualTime.config
      .withFallback(EventSourcedBehaviorTestKit.config)
  ) with AnyFlatSpecLike
    with BeforeAndAfterEach {

  private type Kit = EventSourcedBehaviorTestKit[
    DeveloperBehavior.Command,
    DeveloperBehavior.Event,
    DeveloperBehavior.State
  ]

  private val manualTime = ManualTime()

  private val workFactor = 10
  private val restFactor = 5
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

  private def addTask(task: Task, kit: Kit = developerTestKit) =
    kit.runCommand(Command.AddTask(task, _))

  private def queueTask(task: Task, kit: Kit = developerTestKit): TaskWithId = {
    val result = addTask(task, kit)
    val id = result.replyOfType[Replies.TaskQueued].id
    TaskWithId(task, id)
  }

  private def calculateWorkTime(difficulty: Int): FiniteDuration =
    DeveloperBehavior.calculateTime(difficulty, workFactor)

  private def calculateRestTime(difficulty: Int): FiniteDuration =
    DeveloperBehavior.calculateTime(difficulty, restFactor)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    developerTestKit.clear()
  }

  /** Разработчик начинает в свободном состоянии */
  "The developer" should "start in a free state" in {
    val state = developerTestKit.getState()
    state shouldEqual State.Free
  }

  /** Когда разработчик свободен, он принимает задачу в работу */
  "The developer" should "accept the task he's given when he's free" in {
    val task = Task(5)
    val result = addTask(task)
    val state = result.stateOfType[Working]
    state.task shouldEqual task
  }

  /** Когда разработчик свободен, то при получении задачи
   * он присваивает ей идентификатор и отправляет его в ответе */
  "The developer" should "reply with a Task Started message when he's free" in {
    val task = Task(15)
    val result = addTask(task)
    val reply = result.replyOfType[Replies.TaskStarted]
    reply.id should not be null
    result.stateOfType[Working].taskId shouldEqual reply.id
  }

  /** До выполнения задачи разработчик работает */
  "The developer" should "work until the task is done" in {
    val task = Task(20)
    val workTime = calculateWorkTime(task.difficulty)

    addTask(task)

    manualTime.timePasses(workTime - 1.millis)
    inside(developerTestKit.getState()) {
      case working: Working => working.task shouldEqual task
    }

    manualTime.timePasses(1.millis)
    developerTestKit.getState() should not be a [State.Working]
  }

  /** Завершив задачу, разработчик делает перерыв */
  "The developer" should "rest after completing a task" in {
    val task = Task(50)
    val workTime = calculateWorkTime(task.difficulty)

    addTask(task)

    manualTime.timePasses(workTime)
    inside(developerTestKit.getState()) {
      case resting: State.Resting =>
        resting.lastCompleted.task shouldEqual task
    }
  }

  /** Перерыв длится строго отведённое время */
  "The developer" should "only rest for a designated time period" in {
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
  "The developer" should "reply with an identifier after receiving a new task while working" in {
    val currentTask = Task(100).withRandomId
    val newTask = Task(10)
    developerTestKit.initialize(Event.TaskStarted(currentTask))
    val result = addTask(newTask, developerTestKit)
    val reply = result.replyOfType[Replies.TaskQueued]
    reply.id should not be null
  }

  /** Когда разработчик работает над задачей, новые задачи отправляются в очередь */
  "The developer" should "queue new tasks while working" in {
    val firstTask = Task(100)
    val secondTask = Task(50)
    val thirdTask = Task(25)

    val firstResult = addTask(firstTask, developerTestKit)
    val secondResult = addTask(secondTask, developerTestKit)
    val thirdResult = addTask(thirdTask, developerTestKit)

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
  "The task queue" should "remain the same when a task is finished" in {
    val firstTask = Task(100)
    val secondTask = Task(50)
    val thirdTask = Task(25)

    addTask(firstTask, developerTestKit)

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
  "The developer" should "take the first task from the queue when he's finished resting" in {
    val firstTask = Task(100)
    val secondTask = Task(90)
    val thirdTask = Task(25)

    addTask(firstTask, developerTestKit)

    val secondTaskWithId = queueTask(secondTask)
    val thirdTaskWithId = queueTask(thirdTask)

    val workTime = calculateWorkTime(firstTask.difficulty)
    val restTime = calculateRestTime(firstTask.difficulty)

    manualTime.timePasses(workTime)
    manualTime.timePasses(restTime)

    inside(developerTestKit.getState()) {
      case Working(currentTask, taskQueue) =>
        currentTask shouldEqual secondTaskWithId
        taskQueue should contain theSameElementsInOrderAs Seq(thirdTaskWithId)
    }
  }

  /** Если задач в очереди нет, после отдыха разработчик возвращается в свободное состояние */
  "The developer" should "be free after resting if there are no more tasks in the queue" in {
    val task = Task(50)
    val workTime = calculateWorkTime(task.difficulty)
    val restTime = calculateRestTime(task.difficulty)

    addTask(task)

    manualTime.timePasses(workTime)
    manualTime.timePasses(restTime)

    developerTestKit.getState() shouldEqual State.Free
  }

  /** Если разработчик отдыхает, новые задачи ставятся в очередь */
  "The developer" should "queue tasks while resting" in {
    val initialTask = Task(1)
    val workTime = calculateWorkTime(initialTask.difficulty)

    addTask(initialTask, developerTestKit)

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
  "The developer actor" should "start the work timer when completing recovery in a Working state" in {
    val taskWithId = TaskWithId(Task(50), UUID.fromString("92ac4c4b-622f-44ba-b331-f1cf40a27c58"))
    val workTime = calculateWorkTime(taskWithId.task.difficulty)

    developerTestKit.initialize(Event.TaskStarted(taskWithId))
    developerTestKit.restart()

    manualTime.timePasses(workTime - 1.millis)
    developerTestKit.getState() shouldBe a [State.Working]

    manualTime.timePasses(1.millis)
    developerTestKit.getState() shouldBe a [State.Resting]
  }

  /** Если актор упал в состоянии отдыха, соответствующий таймер запускается по новой */
  "The developer actor" should "start the rest timer when completing recovery in a Resting state" in {
    val taskWithId = TaskWithId(Task(10), UUID.fromString("b807f5ff-6066-454e-8d53-2a90a3941cc4"))
    val restTime = calculateRestTime(taskWithId.task.difficulty)

    developerTestKit.initialize(Event.TaskStarted(taskWithId), Event.TaskFinished(taskWithId))
    developerTestKit.restart()

    manualTime.timePasses(restTime - 1.millis)
    developerTestKit.getState() shouldBe a [State.Resting]

    manualTime.timePasses(1.millis)
    developerTestKit.getState() shouldBe State.Free
  }

  /** После отдыха разработчик выполняет следующую задачу из очереди до конца */
  "After resting the developer" should "fully complete the next task in the queue" in {
    val lastCompleted = TaskWithId(Task(12), UUID.fromString("6bf0af94-4ee3-4857-9a38-3e31e529b37d"))
    val taskQueue = TaskWithId(Task(35), UUID.fromString("ba5be578-9af1-44a6-9b8b-0a11c340237b")) ::
      TaskWithId(Task(19), UUID.fromString("da2b386f-a53e-44a8-b943-8e7491d1010e")) ::
      Nil
    val restTime = calculateRestTime(lastCompleted.task.difficulty)
    val nextTask = taskQueue.head
    val workTime = calculateWorkTime(nextTask.task.difficulty)

    developerTestKit.initialize(State.Resting(lastCompleted, taskQueue))

    manualTime.timePasses(restTime)
    developerTestKit.getState() shouldEqual State.Working(nextTask, taskQueue.tail)
    manualTime.timePasses(workTime - 1.millis)
    developerTestKit.getState() shouldEqual State.Working(nextTask, taskQueue.tail)
    manualTime.timePasses(1.millis)
    developerTestKit.getState() shouldEqual State.Resting(nextTask, taskQueue.tail)
  }

}
