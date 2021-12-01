package utils.responses

import core.{BaseServiceTest, Project, Task, UserId}
import core.projects.{InMemoryProjectStorage, ProjectService, ProjectValidator}
import core.tasks.{InMemoryTaskStorage, TaskService, TaskValidator}

import java.util.UUID
import scala.util.Random

class ProjectInfoResponseTest extends BaseServiceTest {
  "ProjectInfoResponse" when {

    "findLastUpdate" should {

      "find the task that starts the latest" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- TaskStorage.saveTask(testTask1)
          _ <- TaskStorage.saveTask(testTask2)
        } yield ProjectsInfoResponse.findLastUpdate(ProjectInfoResponseWithoutTime(testProject1, Seq(testTask1, testTask2))) shouldBe 1637336456499L)
      }
    }

    "apply" should {
      "sort projects by tasks that start the latest increasing" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
          _ <- TaskStorage.saveTask(testTask1)
          _ <- TaskStorage.saveTask(testTask2)
          _ <- TaskStorage.saveTask(testTask3)
        } yield ProjectsInfoResponse(Seq(testProject1, testProject2), Some(true), TaskService).equals(Seq(ProjectInfoResponseWithoutTime(testProject2, Seq(testTask3)), ProjectInfoResponseWithoutTime(testProject1, Seq(testTask1, testTask2)))) shouldBe true)
      }

      "sort projects by tasks that start the latest decreasing" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
          _ <- TaskStorage.saveTask(testTask1)
          _ <- TaskStorage.saveTask(testTask2)
          _ <- TaskStorage.saveTask(testTask3)
        } yield ProjectsInfoResponse(Seq(testProject1, testProject2), Some(false), TaskService) shouldBe Seq(ProjectInfoResponseWithoutTime(testProject1, Seq(testTask1, testTask2)), ProjectInfoResponseWithoutTime(testProject2, Seq(testTask3))))
      }
    }
  }

  trait Context {
    val TaskStorage = new InMemoryTaskStorage()
    val ProjectStorage = new InMemoryProjectStorage()
    val ProjectValidator = new ProjectValidator(ProjectStorage)
    val ProjectService = new ProjectService(ProjectStorage, ProjectValidator)
    val TaskValidator = new TaskValidator(TaskStorage, ProjectValidator)
    val TaskService = new TaskService(TaskStorage, TaskValidator, ProjectValidator)

    val testOwner1: String = "30313317-59c1-4a53-965e-20baf6f2e1f3"

    val testProject1: Project = testProject(UUID.randomUUID().toString, UUID.randomUUID().toString, Random.nextLong(1637336456494L), testOwner1)
    val testProject2: Project = testProject(UUID.randomUUID().toString, UUID.randomUUID().toString, Random.nextLong(1637336456494L), testOwner1)

    def testProject(id: String, name: String, startPointer: Long, owner: UserId):Project = Project(id, name,startPointer, 0L, owner)

    val testTask1: Task = testTask(UUID.randomUUID().toString, testProject1.id, 1637336456494L,2, 12L, "desc")
    val testTask2: Task = testTask(UUID.randomUUID().toString, testProject1.id, 1637336456499L,2, 12L, "desc")
    val testTask3: Task = testTask(UUID.randomUUID().toString, testProject2.id, 1637336456494L,2, 12L, "desc")
    def testTask(id: String, projectId: String, startPointer: Long, volume: Int, workingTime: Long, desc: String): Task = Task(id, projectId, startPointer, volume, workingTime, desc)
  }
}
