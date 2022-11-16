package rr64.developer.infrastructure

import akka.actor.testkit.typed.scaladsl.{ManualTime, ScalaTestWithActorTestKit}
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import akka.persistence.typed.PersistenceId
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.Task
import rr64.developer.infrastructure.DeveloperBehavior.Replies.TaskQueued
import rr64.developer.infrastructure.DeveloperBehavior.State.Working
import rr64.developer.infrastructure.DeveloperBehavior._

import java.util.UUID
import scala.concurrent.duration.DurationInt

class DeveloperBehaviorTestSuite
  extends ScalaTestWithActorTestKit(
    ManualTime.config
      .withFallback(EventSourcedBehaviorTestKit.config)
  )
  with AnyFlatSpecLike
  with BeforeAndAfterEach {

  private type Kit = EventSourcedBehaviorTestKit[
    DeveloperBehavior.Command,
    DeveloperBehavior.Event,
    DeveloperBehavior.State
  ]

  private def createTestKit(
    persistenceId: String,
    workFactor: Int,
    restFactor: Int
  ): Kit = {
    EventSourcedBehaviorTestKit(
      system = system,
      behavior = DeveloperBehavior(
        persistenceId = PersistenceId.ofUniqueId(persistenceId),
        workFactor = workFactor,
        restFactor = restFactor
      ),
      SerializationSettings.disabled
    )
  }

  private val manualTime = ManualTime()

  private val workFactor = 10
  private val restFactor = 5
  private val developerTestKit = createTestKit("dev-test", workFactor, restFactor)

  private def addTask(task: Task, kit: Kit = developerTestKit) =
    kit.runCommand(AddTask(task, _))

  private def queueTask(task: Task, kit: Kit = developerTestKit): TaskWithId = {
    val result = addTask(task, kit)
    val id = result.replyOfType[TaskQueued].id
    TaskWithId(task, id)
  }

  private def workTimeMs(difficulty: Int): Int = difficulty * workFactor
  private def restTimeMs(difficulty: Int): Int = difficulty * restFactor

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    developerTestKit.clear()
  }

  /** Разработчик начинает в свободном состоянии */
  "The developer" should "start in a free state" in {
    val state = developerTestKit.getState()
    state shouldBe a [State.Free]
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
    val task = Task(5)
    val result = addTask(task)
    val reply = result.replyOfType[Replies.TaskStarted]
    reply.id should not be null
    result.stateOfType[Working].taskId shouldEqual reply.id
  }

  /** До выполнения задачи разработчик работает */
  "The developer" should "work until the task is done" in {
    val difficulty = 10
    val task = Task(difficulty)
    val workTime = workTimeMs(difficulty)

    addTask(task)

    manualTime.timePasses((workTime - 1).millis)

    inside(developerTestKit.getState()) {
      case working: Working => working.task shouldEqual task
    }

    manualTime.timePasses(1.millis)

    developerTestKit.getState() should not be a [State.Working]
  }

  /** Завершив задачу, разработчик делает перерыв */
  "The developer" should "rest after completing a task" in {
    val difficulty = 50
    val task = Task(difficulty)
    val workTime = workTimeMs(difficulty)
    val restingTime = restTimeMs(difficulty)

    addTask(task)

    manualTime.timePasses(workTime.millis)

    inside(developerTestKit.getState()) {
      case resting: State.Resting =>
        resting.millis shouldEqual restingTime
    }
  }

  /** Перерыв длится строго отведённое время */
  "The developer" should "only rest for a designated time period" in {
    val difficulty = 50
    val task = Task(difficulty)
    val workTime = workTimeMs(difficulty)
    val restingTime = restTimeMs(difficulty)

    addTask(task)

    manualTime.timePasses(workTime.millis)
    manualTime.timePasses(restingTime.millis)

    developerTestKit.getState() should not be a [State.Resting]
  }

  /** Когда разработчик работает над задачей,
   * то при получении новой задачи он присваивает ей идентификатор
   * и отправляет его в ответе */
  "The developer" should "reply with an identifier after receiving a new task while working" in {
    val currentTask = TaskWithId(Task(100), UUID.randomUUID())
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

    val workTime = workTimeMs(firstTask.difficulty)

    manualTime.timePasses(workTime.millis)

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

    val workTime = workTimeMs(firstTask.difficulty)
    val restTime = restTimeMs(firstTask.difficulty)

    manualTime.timePasses(workTime.millis)
    manualTime.timePasses(restTime.millis)

    inside(developerTestKit.getState()) {
      case Working(currentTask, taskQueue) =>
        currentTask shouldEqual secondTaskWithId
        taskQueue should contain theSameElementsInOrderAs Seq(thirdTaskWithId)
    }
  }

  /** Если задач в очереди нет, после отдыха разработчик возвращается в свободное состояние */
  "The developer" should "be free after resting if there are no more tasks in the queue" in {
    val difficulty = 50
    val task = Task(difficulty)
    val workTime = workTimeMs(difficulty)
    val restingTime = restTimeMs(difficulty)

    addTask(task)

    manualTime.timePasses(workTime.millis)
    manualTime.timePasses(restingTime.millis)

    developerTestKit.getState() shouldBe a [State.Free]
  }

  /** Если разработчик отдыхает, новые задачи ставятся в очередь */
  "The developer" should "queue tasks while resting" in {
    val initialTask = Task(1)
    val workTime = workTimeMs(initialTask.difficulty)

    addTask(initialTask, developerTestKit)

    manualTime.timePasses(workTime.millis)

    developerTestKit.getState() shouldBe a [State.Resting]

    val taskWithId1 = queueTask(Task(10))
    val taskWithId2 = queueTask(Task(5))

    inside(developerTestKit.getState()) {
      case resting: State.Resting =>
        resting.taskQueue should contain theSameElementsInOrderAs Seq(taskWithId1, taskWithId2)
    }
  }

}
