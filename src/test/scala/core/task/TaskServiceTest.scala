package core.task

import core.{BaseServiceTest, Project, ProjectDataUpdate, Task, TaskDataUpdate, UserId}
import core.projects.{InMemoryProjectStorage, ProjectService, ProjectValidator}
import core.tasks.{InMemoryTaskStorage, TaskService, TaskValidator}
import utils.responses.{NameOccupiedException, NoResourceException, NotAuthorisedException, TimeConflictException}

import java.util.UUID
import scala.util.Random

class TaskServiceTest  extends BaseServiceTest{
  "TaskService" when {
    "deleteTask" should {
      "delete created task" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- TaskService.createTask(testTask1)
          _ <- TaskService.deleteTask(testTask1.id, testOwner1)
          task <- TaskService.getTask(testTask1.id)
        } yield task.get.endPointer.equals(0L)  shouldBe false)
      }
    }

    "getProjectTasks" should {
      "return projects tasks when user is authorised" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- TaskService.createTask(testTask1)
          _ <- TaskService.createTask(testTask2)
        } yield TaskService.getProjectTasks(testProject1.id, testOwner1).size == 2 shouldBe true)
      }

      "throw not-authorised-exception when user is not authorised" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- TaskService.createTask(testTask1)
          _ <- TaskService.createTask(testTask2)
        } yield  intercept[Exception] {
          TaskService.getProjectTasks(testProject1.id, testOwner2)
        }.isInstanceOf[NotAuthorisedException] shouldBe  true)
      }
    }

    "createTask" should {
      "throw no-resource-exception when project has been deleted" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectService.deleteProject(testProject1.id, testOwner1)
        } yield  intercept[Exception] {
          TaskService.createTask(testTask1)
        }.isInstanceOf[NoResourceException] shouldBe  true)
      }

      "throw no-resource-exception when project does not exists" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject2)
        } yield  intercept[Exception] {
          TaskService.createTask(testTask1)
        }.isInstanceOf[NoResourceException] shouldBe  true)
      }
      "throw time-conflict-exception when new task overlaps with the added ones" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- TaskService.createTask(Task(UUID.randomUUID().toString, testProject1.id, 12300L, 12, 15L, "desc"))
          _ <- TaskService.createTask(Task(UUID.randomUUID().toString, testProject1.id, 12430L, 12, 15L, "desc"))
        } yield  intercept[Exception] {
          TaskService.createTask(Task(UUID.randomUUID().toString, testProject1.id, 12310L, 12, 25L, "desc"))
        }.isInstanceOf[TimeConflictException] shouldBe  true)
      }
    }

    //final case class TaskDataUpdate(startPointer: Option[Long] = None, volume: Option[Int] = None, workingTime: Option[Long],  desc: Option[String] = None
    "updateTask" should {
      "update the task" in new Context {
        val taskDataUpdate: TaskDataUpdate = TaskDataUpdate(Some(123L), Some(9), Some(13L), Some("new-desc"))
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject1)
          _ <- TaskService.createTask(testTask1)
          _ <- TaskService.updateTask(testTask1.id, taskDataUpdate, testOwner1)
          task <- TaskService.getTask(testTask1.id)
        } yield (task.get.desc.equals("new-desc") &&
          task.get.startPointer.equals(123L) &&
          task.get.volume.equals(9) &&
          task.get.workingTime.equals(13L)) shouldBe true)
      }

      "throw not-authorised-exception when user is not authorised to perform the operation" in new Context {
        val taskDataUpdate: TaskDataUpdate = TaskDataUpdate(Some(123L), Some(9), Some(13L), Some("new-desc"))
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject1)
          _ <- TaskService.createTask(testTask1)
        } yield intercept[Exception] {
          TaskService.updateTask(testTask1.id, taskDataUpdate, testOwner2)
        }.isInstanceOf[NotAuthorisedException] shouldBe  true)
      }

      "throw no resource exception when the project has been deleted" in new Context {
        val taskDataUpdate: TaskDataUpdate = TaskDataUpdate(Some(123L), Some(9), Some(13L), Some("new-desc"))
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject1)
          _ <- TaskService.createTask(testTask1)
          _ <- ProjectService.deleteProject(testProject1.id, testOwner1)
        } yield intercept[Exception] {
          TaskService.updateTask(testTask1.id, taskDataUpdate, testOwner1)
        }.isInstanceOf[NoResourceException] shouldBe  true)
      }

      "throw time conflict exception when potential time update causes conflicts" in new Context {
        val addedTask1:Task = Task(UUID.randomUUID().toString, testProject1.id, 12300L, 12, 15L, "desc")
        val addedTask2:Task = Task(UUID.randomUUID().toString, testProject1.id, 12330L, 12, 15L, "desc")
        val taskDataUpdate: TaskDataUpdate = TaskDataUpdate(Some(12310L), Some(9), Some(13L), Some("new-desc"))
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject1)
          _ <- TaskService.createTask(addedTask1)
          _ <- TaskService.createTask(addedTask2)
        } yield intercept[Exception] {
          TaskService.updateTask(addedTask2.id, taskDataUpdate, testOwner1)
        }.isInstanceOf[TimeConflictException] shouldBe  true)
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
