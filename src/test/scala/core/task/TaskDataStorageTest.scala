package core.task

import core.{BaseServiceTest, Project, Task, UserId}
import core.tasks.{InMemoryTaskStorage, JdbcTaskDataStorage, TaskDataStorage}
import utils.InMemoryPostgresStorage

import java.util.UUID
import scala.util.Random

class JdbcTaskStorageTest extends TaskStorageSpec {
  override def TaskStorageBuilder(): TaskDataStorage =
    new JdbcTaskDataStorage(InMemoryPostgresStorage.databaseConnector)
}

class InMemoryTaskStorageTest extends TaskStorageSpec {
  override def TaskStorageBuilder(): TaskDataStorage =
    new InMemoryTaskStorage()
}

abstract class TaskStorageSpec extends BaseServiceTest {

  def TaskStorageBuilder(): TaskDataStorage

  "TaskStorage" when {

    "getTask" should {

      "return Task by id" in new Context {
        awaitForResult(for {
          _ <- TaskStorage.saveTask(testTask1)
          _ <- TaskStorage.saveTask(testTask2)
          maybeTask <- TaskStorage.getTask(testTask1.id)
        } yield maybeTask shouldBe Some(testTask1))
      }

      "return None if Task not found" in new Context {
        awaitForResult(for {
          maybeTask <- TaskStorage.getTask("xxxxxxxxx")
        } yield maybeTask shouldBe None)
      }

    }

    "getProjectTasks" should {

      "return all user's Tasks from database" in new Context {
        awaitForResult(for {
          _ <- TaskStorage.saveTask(testTask1)
          _ <- TaskStorage.saveTask(testTask2)
          _ <- TaskStorage.saveTask(testTask3)
          tasks <- TaskStorage.getProjectTasks(testProjectId1)
        } yield (tasks.contains(testTask1) && tasks.contains(testTask2) && !tasks.contains(testTask3)) shouldBe true)
      }

    }

    "saveTask" should {

      "save Task to database" in new Context {
        awaitForResult(for {
          _ <- TaskStorage.saveTask(testTask1)
          maybeTask <- TaskStorage.getTask(testTask1.id)
        } yield maybeTask shouldBe Some(testTask1))
      }

      "overwrite Task if it exists" in new Context {
        awaitForResult(for {
          _ <- TaskStorage.saveTask(testTask1.copy(endPointer = 1637336456495L))
          _ <- TaskStorage.saveTask(testTask1)
          maybeTask <- TaskStorage.getTask(testTask1.id)
        } yield maybeTask shouldBe Some(testTask1))
      }
    }

  }

  trait Context {
    val TaskStorage: TaskDataStorage = TaskStorageBuilder()
    val testProjectId1: String = "cba7ad1a-f25f-4cfe-81e5-a895d9f178b1"
    val testProjectId2: String = "ac5658a9-8856-4e76-b187-d9951ddfbc69"

    val testTask1: Task = testTask(UUID.randomUUID().toString, testProjectId1, Random.nextLong(1637336456494L),2, 12L, "desc")
    val testTask2: Task = testTask(UUID.randomUUID().toString, testProjectId1, Random.nextLong(1637336456494L),2, 12L, "desc")
    val testTask3: Task = testTask(UUID.randomUUID().toString, testProjectId2, Random.nextLong(1637336456494L),2, 12L, "desc")
    def testTask(id: String, projectId: String, startPointer: Long, volume: Int, workingTime: Long, desc: String): Task = Task(id, projectId, startPointer, volume, workingTime, desc)
  }

}
