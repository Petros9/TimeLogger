package core.project

import core.{BaseServiceTest, Project, UserId}
import core.projects.{InMemoryProjectStorage, ProjectService, ProjectValidator}

import java.util.UUID
import scala.util.Random
/*
class ProjectServiceTest extends BaseServiceTest {

  "ProjectService" when {

    "getProjects" should {

      "return all stored Projects" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
          projects <- ProjectService.getProjects()
        } yield projects shouldBe Seq(testProject1, testProject2))
      }

    }

    "getProject" should {

      "return Project by id" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
          maybeProject <- ProjectService.getProject(testProjectId1)
        } yield maybeProject shouldBe Some(testProject1))
      }

      "return None if Project not exists" in new Context {
        awaitForResult(for {
          _ <- ProjectStorage.saveProject(testProject1)
          _ <- ProjectStorage.saveProject(testProject2)
          maybeProject <- ProjectService.getProject("wrongId")
        } yield maybeProject shouldBe None)
      }

    }

    "createProject" should {

      "store Project" in new Context {
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject1)
          maybeProject <- ProjectStorage.getProject(testProjectId1)
        } yield maybeProject shouldBe Some(testProject1))
      }

    }

    "updateProject" should {

      "merge Project with partial update" in new Context {
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject1)
          _ <- ProjectService.updateProject(testProjectId1, ProjectUpdate(Some("test"), Some("test")))
          maybeProject <- ProjectStorage.getProject(testProjectId1)
        } yield maybeProject shouldBe Some(testProject1.copy(firstName = "test", lastName = "test")))
      }

      "return None if Project is not exists" in new Context {
        awaitForResult(for {
          maybeProject <- ProjectService.updateProject(testProjectId1, ProjectUpdate(Some("test"), Some("test")))
        } yield maybeProject shouldBe None)
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

}*/
