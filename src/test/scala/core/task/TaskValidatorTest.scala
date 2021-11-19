package core.task

import core.projects.{InMemoryProjectStorage, ProjectValidator}
import core.tasks.{InMemoryTaskStorage, TaskDataStorage, TaskService, TaskValidator}
import core.{BaseServiceTest, Project, Task, UserId}

import java.util.UUID
import scala.util.Random

class TaskValidatorTest extends BaseServiceTest {

  "ProjectService" when {

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



    }

    "timeDoesNotOverlap" should {


    }

    "getTaskToBeUpdated" should {


    }

    "isNotDeleted" should {

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
