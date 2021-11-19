package core.project

import core.{BaseServiceTest, Project, UserId}
import core.projects.{InMemoryProjectStorage, ProjectService, ProjectValidator}

import java.util.UUID
import scala.util.Random

class ProjectValidatorTest extends BaseServiceTest {

  "ProjectService" when {

    "isOwner" should {

      "return true if correct project owner is provided" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
        } yield ProjectValidator.isOwner(testProjectId1, testOwner1) shouldBe true)
      }

      "return false if incorrect project owner is provided" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
        } yield ProjectValidator.isOwner(testProjectId1, testOwner2) shouldBe false)
      }
    }

    "nameIsFreeToBeUsed" should {

      "return false if project name is occupied" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
        } yield ProjectValidator.nameIsFreeToBeUsed(Option(testProjectName1)) shouldBe false)
      }

      "return true if project name is available" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
        } yield ProjectValidator.nameIsFreeToBeUsed(Option("xxxxxxxx")) shouldBe true)
      }

      "return true if new project name is not provided" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
        } yield ProjectValidator.nameIsFreeToBeUsed(None) shouldBe true)
      }

    }

    "isNotDeleted" should {

      "return true if the project has not been deleted" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
        } yield ProjectValidator.isNotDeleted(testProjectId1) shouldBe true)
      }

      "return true if the project has  been deleted" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectService.deleteProject(testProjectId1, testOwner1)
        } yield ProjectValidator.isNotDeleted(testProjectId1) shouldBe false)
      }
    }
  }

  trait Context {
    val ProjectStorage = new InMemoryProjectStorage()
    val ProjectValidator = new ProjectValidator(ProjectStorage)
    val ProjectService = new ProjectService(ProjectStorage, ProjectValidator)

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
