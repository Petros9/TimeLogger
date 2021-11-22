package core.task

import core.projects.{InMemoryProjectStorage, ProjectValidator}
import core.tasks.{InMemoryTaskStorage, TaskService, TaskValidator}
import core.{BaseServiceTest, Project, Task, UserId}

import java.util.UUID
import scala.util.Random

class TaskValidatorTest extends BaseServiceTest {

  "TaskValidator" when {

    "isOwner" should {

      "return true if correct task owner is provided" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
          _ <- TaskStorage.saveTask(testTask1)
          _ <- TaskStorage.saveTask(testTask3)
        } yield TaskValidator.isOwner(testTask1.id, testOwner1) shouldBe true)
      }

      "return false if incorrect task owner is provided" in new Context {
        awaitForResult(for {
          _ <- TaskStorage.saveTask(testTask1)
          _ <- TaskStorage.saveTask(testTask3)
        } yield ProjectValidator.isOwner(testTask1.id, testOwner2) shouldBe false)
      }
    }

    "boundariesOverlap" should {

      "return true when a new task begins before the added one and ends after the added one beginnings" in new Context {
        val addedTaskStartingPoint: Long = 12300L
        val addedTaskEndingPoint: Long = addedTaskStartingPoint + 12L

        val newTaskStartingPoint: Long = 12297L
        val newTaskEndingPoint: Long = newTaskStartingPoint + 12L
        assert(TaskValidator.boundariesOverlap(newTaskStartingPoint, newTaskEndingPoint, addedTaskStartingPoint, addedTaskEndingPoint), true)
      }

      "return true when a new task begins after the added one begins but before it's ending" in new Context {
        val addedTaskStartingPoint: Long = 12300L
        val addedTaskEndingPoint: Long = addedTaskStartingPoint + 12L

        val newTaskStartingPoint: Long = 12311L
        val newTaskEndingPoint: Long = newTaskStartingPoint + 12L
        assert(TaskValidator.boundariesOverlap(newTaskStartingPoint, newTaskEndingPoint, addedTaskStartingPoint, addedTaskEndingPoint), true)
      }

      "return true when a new task is about to be surrounded by the added one" in new Context {
        val addedTaskStartingPoint: Long = 12300L
        val addedTaskEndingPoint: Long = addedTaskStartingPoint + 12L

        val newTaskStartingPoint: Long = 12301L
        val newTaskEndingPoint: Long = newTaskStartingPoint + 1L
        assert(TaskValidator.boundariesOverlap(newTaskStartingPoint, newTaskEndingPoint, addedTaskStartingPoint, addedTaskEndingPoint), true)
      }


      "return true when a new task is about to surround the added one" in new Context {
        val addedTaskStartingPoint: Long = 12300L
        val addedTaskEndingPoint: Long = addedTaskStartingPoint + 12L

        val newTaskStartingPoint: Long = 12297L
        val newTaskEndingPoint: Long = newTaskStartingPoint + 20L
        assert(TaskValidator.boundariesOverlap(newTaskStartingPoint, newTaskEndingPoint, addedTaskStartingPoint, addedTaskEndingPoint), true)
      }


      "return false when a new task begins after the other ends" in new Context {
        val addedTaskStartingPoint: Long = 12300L
        val addedTaskEndingPoint: Long = addedTaskStartingPoint + 12L

        val newTaskStartingPoint: Long = 12315L
        val newTaskEndingPoint: Long = newTaskStartingPoint + 12L
        assert(!TaskValidator.boundariesOverlap(newTaskStartingPoint, newTaskEndingPoint, addedTaskStartingPoint, addedTaskEndingPoint), true)
      }

      "return false when a new task ends before the other begins" in new Context {
        val addedTaskStartingPoint: Long = 12300L
        val addedTaskEndingPoint: Long = addedTaskStartingPoint + 12L

        val newTaskStartingPoint: Long = 12297L
        val newTaskEndingPoint: Long = newTaskStartingPoint + 1
        assert(!TaskValidator.boundariesOverlap(newTaskStartingPoint, newTaskEndingPoint, addedTaskStartingPoint, addedTaskEndingPoint), true)
      }
    }

    "timeDoesNotOverlap" should {
      "return false when a new task overlaps with the added tasks" in new Context {
        val testProject: Project = testProject(UUID.randomUUID().toString, UUID.randomUUID().toString, Random.nextLong(1637336456494L), testOwner1)
        val addedTask1: Task = Task(UUID.randomUUID().toString, testProject.id, 12300L, 12, 15L, "desc")
        val addedTask2: Task = Task(UUID.randomUUID().toString, testProject.id, 12330L, 12, 15L, "desc")

        val newTask: Task = Task(UUID.randomUUID().toString, testProject.id, 12310L, 12, 25L, "desc")
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject)
          _ <- TaskStorage.saveTask(addedTask1)
          _ <- TaskStorage.saveTask(addedTask2)
        } yield TaskValidator.timeDoesNotOverlap(newTask.startPointer, newTask.endPointer, newTask.projectId, newTask.id) shouldBe false)
      }

      "return false when a new task overlaps with the added task" in new Context {
        val testProject: Project = testProject(UUID.randomUUID().toString, UUID.randomUUID().toString, Random.nextLong(1637336456494L), testOwner1)
        val addedTask1: Task = Task(UUID.randomUUID().toString, testProject.id, 12300L, 12, 15L, "desc")
        val addedTask2: Task = Task(UUID.randomUUID().toString, testProject.id, 12430L, 12, 15L, "desc")

        val newTask: Task = Task(UUID.randomUUID().toString, testProject.id, 12310L, 12, 25L, "desc")
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject)
          _ <- TaskStorage.saveTask(addedTask1)
          _ <- TaskStorage.saveTask(addedTask2)
        } yield TaskValidator.timeDoesNotOverlap(newTask.startPointer, newTask.endPointer, newTask.projectId, newTask.id) shouldBe false)
      }

      "return true when a new task does not overlap with the added tasks" in new Context {
        val testProject: Project = testProject(UUID.randomUUID().toString, UUID.randomUUID().toString, Random.nextLong(1637336456494L), testOwner1)
        val addedTask1: Task = Task(UUID.randomUUID().toString, testProject.id, 12300L, 12, 15L, "desc")
        val addedTask2: Task = Task(UUID.randomUUID().toString, testProject.id, 12430L, 12, 15L, "desc")

        val newTask: Task = Task(UUID.randomUUID().toString, testProject.id, 12350L, 12, 25L, "desc")
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject)
          _ <- TaskStorage.saveTask(addedTask1)
          _ <- TaskStorage.saveTask(addedTask2)
        } yield TaskValidator.timeDoesNotOverlap(newTask.startPointer, newTask.endPointer, newTask.projectId, newTask.id) shouldBe true)
      }
    }

    "getTaskToBeUpdated" should {
      "return proper task to get it updated" in new Context {
        awaitForResult(for {
          _ <- TaskStorage.saveTask(testTask1)
        } yield TaskValidator.getTaskToBeUpdated(testTask1.id).get shouldBe testTask1)
      }

    }

    "isNotDeleted" should {
      "return true if the task has not been deleted" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
          _ <- TaskStorage.saveTask(testTask1)
          _ <- TaskStorage.saveTask(testTask3)
        } yield TaskValidator.isNotDeleted(testTask1.id) && TaskValidator.isNotDeleted(testTask3.id) shouldBe true)
      }

      "return false if the task has been deleted" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject2)
          _ <- TaskStorage.saveTask(testTask3)
          _ <- TaskService.deleteTask(testTask3.id, testOwner2)
        } yield TaskValidator.isNotDeleted(testTask3.id) shouldBe false)
      }
    }
  }

  trait Context {
    val TaskStorage = new InMemoryTaskStorage()
    val ProjectStorage = new InMemoryProjectStorage()
    val ProjectValidator = new ProjectValidator(ProjectStorage)
    val TaskValidator = new TaskValidator(TaskStorage, ProjectValidator)
    val TaskService = new TaskService(TaskStorage, TaskValidator, ProjectValidator)

    val testOwner1: String = "30313317-59c1-4a53-965e-20baf6f2e1f3"
    val testOwner2: String = "c145395f-8f73-44db-a1e1-156300bf6a71"

    val testProject1: Project = testProject(UUID.randomUUID().toString, UUID.randomUUID().toString, Random.nextLong(1637336456494L), testOwner1)
    val testProject2: Project = testProject(UUID.randomUUID().toString, UUID.randomUUID().toString, Random.nextLong(1637336456494L), testOwner2)

    def testProject(id: String, name: String, startPointer: Long, owner: UserId):Project = Project(id, name,startPointer, 0L, owner)

    val testTask1: Task = testTask(UUID.randomUUID().toString, testProject1.id, Random.nextLong(1637336456494L),2, 12L, "desc")
    val testTask2: Task = testTask(UUID.randomUUID().toString, testProject1.id, Random.nextLong(1637336456494L),2, 12L, "desc")
    val testTask3: Task = testTask(UUID.randomUUID().toString, testProject2.id, Random.nextLong(1637336456494L),2, 12L, "desc")
    def testTask(id: String, projectId: String, startPointer: Long, volume: Int, workingTime: Long, desc: String): Task = Task(id, projectId, startPointer, volume, workingTime, desc)
  }

}
