package core.project

import core.projects.{InMemoryProjectStorage, JdbcProjectDataStorage, ProjectDataStorage}
import core.{BaseServiceTest, Project, UserId}
import utils.InMemoryPostgresStorage

import java.util.UUID
import scala.util.Random

class JdbcProjectStorageTest extends ProjectStorageSpec {
  override def ProjectStorageBuilder(): ProjectDataStorage =
    new JdbcProjectDataStorage(InMemoryPostgresStorage.databaseConnector)
}

class InMemoryProjectStorageTest extends ProjectStorageSpec {
  override def ProjectStorageBuilder(): ProjectDataStorage =
    new InMemoryProjectStorage()
}

abstract class ProjectStorageSpec extends BaseServiceTest {

  def ProjectStorageBuilder(): ProjectDataStorage

  "ProjectStorage" when {

    "getProject" should {

      "return Project by id" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
          maybeProject <- ProjectStorage.getProject(testProjectId1)
        } yield maybeProject shouldBe Some(testProject1))
      }

      "return None if Project not found" in new Context {
        awaitForResult(for {
          maybeProject <- ProjectStorage.getProject("xxxxxxxxx")
        } yield maybeProject shouldBe None)
      }

    }

    "getProjects" should {

      "return all user's Projects from database" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
          projects <- ProjectStorage.getProjects(testOwner1)
        } yield projects.head.owner.equals(testOwner1) shouldBe true)
      }

    }

    "saveProject" should {

      "save Project to database" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          maybeProject <- ProjectStorage.getProject(testProjectId1)
        } yield maybeProject shouldBe Some(testProject1))
      }

      "overwrite Project if it exists" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1.copy( projectName= "test", endPointer = 1637336456495L))
          _ <- ProjectStorage.saveProject(testProject1)
          maybeProject <- ProjectStorage.getProject(testProjectId1)
        } yield maybeProject shouldBe Some(testProject1))
      }

      "try to give project used name" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          isNameFreeToBeUsed <- ProjectStorage.nameIsFreeToBeUsed(testProjectName1)
        } yield isNameFreeToBeUsed shouldBe Some(testProject1))
      }

      "try to give project unused name" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          isNameFreeToBeUsed <- ProjectStorage.nameIsFreeToBeUsed("asdfgfsjfad")
        } yield isNameFreeToBeUsed shouldBe None)
      }
    }

  }

  trait Context {
    val ProjectStorage: ProjectDataStorage = ProjectStorageBuilder()
    val testProjectId1: String = UUID.randomUUID().toString
    val testProjectId2: String = UUID.randomUUID().toString
    val testProjectName1: String = UUID.randomUUID().toString
    val testProjectName2: String = UUID.randomUUID().toString
    val testProjectStartPointer1: Long = Random.nextLong(1637336456494L)
    val testProjectStartPointer2: Long = Random.nextLong(1637336456494L)
    val endPoint: Long = 0L

    val testOwner1: String = "30313317-59c1-4a53-965e-20baf6f2e1f3"
    val testOwner2: String = "c145395f-8f73-44db-a1e1-156300bf6a71"

    val testProject1: Project = testProject(testProjectId1, testProjectName1, testProjectStartPointer1, testOwner1)
    val testProject2: Project = testProject(testProjectId2, testProjectName2, testProjectStartPointer2, testOwner2)

    def testProject(id: String, name: String, startPointer: Long, owner: UserId):Project = Project(id, name,startPointer, 0L, owner)
  }

}
