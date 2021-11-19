package core.project

import core.{BaseServiceTest, Project, ProjectDataUpdate, ProjectsFilters, UserId}
import core.projects.{InMemoryProjectStorage, ProjectService, ProjectValidator}
import utils.responses.{NameOccupiedException, NoResourceException, NotAuthorisedException}

import java.util.UUID
import scala.util.Random

class ProjectServiceTest extends BaseServiceTest {

  "ProjectService" when {

    "deleteProject" should {

      "delete created project" in new Context {
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject1)
          _ <- ProjectService.deleteProject(testProject1.id, testOwner1)
          project <- ProjectService.getProject(testProject1.id, testOwner1)
        } yield project.get.endPointer.equals(0L)  shouldBe false)
      }

    }

    "idListFilter" should {

      "select project which ids are present in given list" in new Context {
        val projectIds = List(testProject3.id, testProject6.id, testProject1.id)
        val projectFilters: ProjectsFilters = ProjectsFilters(Some(projectIds), None, None, None, None, None)
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject1)
          _ <- ProjectService.createProject(testProject3)
          _ <- ProjectService.createProject(testProject4)
          _ <- ProjectService.createProject(testProject5)
          _ <- ProjectService.createProject(testProject6)
        } yield ProjectService.getProjects(testOwner1, projectFilters).equals(List(testProject1, testProject3, testProject6)) shouldBe true)
      }

      "select all projects when no ids list was given" in new Context {
        val projectFilters: ProjectsFilters = ProjectsFilters(None, None, None, None, None, None)
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject1)
          _ <- ProjectService.createProject(testProject3)
          _ <- ProjectService.createProject(testProject4)
          // case class ProjectsFilters(idList: Option[Seq[ProjectId]], startTime: Option[Long], endTime: Option[Long], deleted: Option[Boolean], creationTimeIncSorting: Option[Boolean], updateTimeIncSorting: Option[Boolean])
        } yield ProjectService.getProjects(testOwner1, projectFilters).equals(List(testProject1, testProject3, testProject4)) shouldBe true)

      }

    }

    "startTimeFilter" should {

      "select projects which are about to start after certain time" in new Context {
        val givenTime: Long = 1234L
        val projectWhichWillStartAfterTheGivenTime1: Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString, 123532131L, 0L, testOwner1)
        val projectWhichWillStartAfterTheGivenTime2: Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString, 1231230L, 0L, testOwner1)
        val projectWhichWillStartBeforeTheGivenTime: Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString, 12L, 0L, testOwner1)
        val projectFilters: ProjectsFilters = ProjectsFilters(None, Some(givenTime), None, None, None, None)
        awaitForResult(for {
          _ <- ProjectService.createProject(projectWhichWillStartBeforeTheGivenTime)
          _ <- ProjectService.createProject(projectWhichWillStartAfterTheGivenTime1)
          _ <- ProjectService.createProject(projectWhichWillStartAfterTheGivenTime2)
        } yield ProjectService.getProjects(testOwner1, projectFilters).equals(List(projectWhichWillStartAfterTheGivenTime1, projectWhichWillStartAfterTheGivenTime2)) shouldBe true)

      }

    }

    "endTimeFilter" should {
      "select projects which are about to end before certain time" in new Context {
        val givenTime: Long = 1234L
        val projectWhichEndedBeforeGivenTime1: Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString, 10L, 11L, testOwner1)
        val projectWhichEndedBeforeGivenTime2: Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString, 10L, 12L, testOwner1)
        val projectWhichEndedAfterGivenTime: Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString, 1231230L, 12331221321L, testOwner1)
        val projectFilters: ProjectsFilters = ProjectsFilters(None, None, Some(givenTime), None, None, None)
        awaitForResult(for {
          _ <- ProjectService.createProject(projectWhichEndedBeforeGivenTime1)
          _ <- ProjectService.createProject(projectWhichEndedBeforeGivenTime2)
          _ <- ProjectService.createProject(projectWhichEndedAfterGivenTime)
        } yield ProjectService.getProjects(testOwner1, projectFilters).equals(List(projectWhichEndedBeforeGivenTime1, projectWhichEndedBeforeGivenTime2)) shouldBe true)
      }
    }

    "deletedFilter" should {
      "select projects which were deleted" in new Context {
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject3)
          _ <- ProjectService.createProject(testProject4)
          _ <- ProjectService.createProject(testProject5)
          _ <- ProjectService.deleteProject(testProject3.id, testOwner1)
          _ <- ProjectService.deleteProject(testProject5.id, testOwner1)
        } yield ProjectService.getProjects(testOwner1, ProjectsFilters(None, None, None, Some(true), None, None)).size
          .equals(2) shouldBe true)
      }

      "select projects which were not deleted" in new Context {
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject3)
          _ <- ProjectService.createProject(testProject4)
          _ <- ProjectService.createProject(testProject5)
          _ <- ProjectService.deleteProject(testProject3.id, testOwner1)
        } yield ProjectService.getProjects(testOwner1, ProjectsFilters(None, None, None, Some(false), None, None))
          .equals(List(testProject4, testProject5)) shouldBe true)
      }

      "select all projects" in new Context {
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject3)
          _ <- ProjectService.createProject(testProject4)
          _ <- ProjectService.createProject(testProject5)
          _ <- ProjectService.deleteProject(testProject3.id, testOwner1)
        } yield ProjectService.getProjects(testOwner1, ProjectsFilters(None, None, None, None, None, None))
          .size == 3 shouldBe true)
      }
    }

    "sortByStartTime" should {
      "sort the result by starting time inc" in new Context {
        val project1: Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString, 67L, 0L, testOwner1)
        val project2: Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString, 43, 0L, testOwner1)
        val project3: Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString, 53L, 12331221321L, testOwner1)
        val projectFilters: ProjectsFilters = ProjectsFilters(None, None, None, None, Some(true), None)
        awaitForResult(for {
          _ <- ProjectService.createProject(project1)
          _ <- ProjectService.createProject(project2)
          _ <- ProjectService.createProject(project3)
        } yield ProjectService.getProjects(testOwner1, projectFilters).equals(List(project2, project3, project1)) shouldBe true)
      }

      "sort the result by starting time desc" in new Context {
        val project1: Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString, 67L, 0L, testOwner1)
        val project2: Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString, 43, 0L, testOwner1)
        val project3: Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString, 53L, 12331221321L, testOwner1)
        val projectFilters: ProjectsFilters = ProjectsFilters(None, None, None, None, Some(false), None)
        awaitForResult(for {
          _ <- ProjectService.createProject(project1)
          _ <- ProjectService.createProject(project2)
          _ <- ProjectService.createProject(project3)
        } yield ProjectService.getProjects(testOwner1, projectFilters).equals(List(project1, project3, project2)) shouldBe true)
      }

    }

    "getProjects" should {

      "select all projects which were created by certain user" in new Context {
        val projectFilters: ProjectsFilters = ProjectsFilters(None, None, None, None, None, None)
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject1)
          _ <- ProjectService.createProject(testProject2)
          _ <- ProjectService.createProject(testProject3)
          _ <- ProjectService.createProject(testProject4)
          _ <- ProjectService.createProject(testProject5)
          _ <- ProjectService.createProject(testProject6)
        } yield ProjectService.getProjects(testOwner1, projectFilters).equals(List(testProject1, testProject3, testProject4, testProject5, testProject6)) shouldBe true)
      }
    }

    "getProject" should {
      "select project with certain id created by provided user" in new Context {
        awaitForResult {
          for {
            _ <- ProjectService.createProject(testProject1)
            project <- ProjectService.getProject(testProject1.id, testOwner1)
          } yield project.get.equals(testProject1) shouldBe true
        }
      }

      "throw not-authorised-exception when non-owner tries to get access to the project" in new Context {
        awaitForResult {
          for {
            _ <- ProjectService.createProject(testProject1)
          } yield intercept[Exception] {
            ProjectService.getProject(testProject1.id, testOwner2)
          }.isInstanceOf[NotAuthorisedException] shouldBe  true
        }
      }

      "throw not authorised exception when somebody tries to get access to a project that does not exists" in new Context {
        awaitForResult {
          for {
            _ <- ProjectService.createProject(testProject1)
          } yield intercept[Exception] {
            ProjectService.getProject(UUID.randomUUID().toString, testOwner2)
          }.isInstanceOf[NotAuthorisedException] shouldBe  true
        }
      }
    }

    "createProject" should {
      "throw name-occupied-exception when somebody tries to create a project which shares its name with another one" in new Context {
        awaitForResult {
          for {
            _ <- ProjectService.createProject(Project(UUID.randomUUID().toString, "test-name",Random.nextLong(1637336456494L), 0L, testOwner1))
          } yield intercept[Exception] {
            ProjectService.createProject(Project(UUID.randomUUID().toString, "test-name",Random.nextLong(1637336456494L), 0L, testOwner1))
          }.isInstanceOf[NameOccupiedException] shouldBe  true
        }
      }
    }

    "updateProject" should {


      "update the project" in new Context {
        val projectDataUpdate: ProjectDataUpdate = ProjectDataUpdate(Some("new-name"), None)
        awaitForResult(for {
          _ <- ProjectService.createProject(testProject1)
          _ <- ProjectService.updateProject(testProject1.id, projectDataUpdate, testOwner1)
          project <- ProjectService.getProject(testProject1.id, testOwner1)
        } yield project.get.projectName.equals("new-name") shouldBe true)
      }

      "throw name-occupied-exception when somebody tries to update a project which shares its name with another one" in new Context {
        awaitForResult {
          for {
            _ <- ProjectService.createProject(testProject1)
            _ <- ProjectService.createProject(Project(UUID.randomUUID().toString, "test-name",Random.nextLong(1637336456494L), 0L, testOwner1))
          } yield intercept[Exception] {
            ProjectService.updateProject(testProject1.id, ProjectDataUpdate(Some("test-name"), None), testOwner1)
          }.isInstanceOf[NameOccupiedException] shouldBe  true
        }
      }

      "throw no-resource-exception when somebody tries to update a project which has been deleted" in new Context {
        awaitForResult {
          for {
            _ <- ProjectService.createProject(testProject6)
            _ <- ProjectService.deleteProject(testProject6.id, testOwner1)
          } yield intercept[Exception] {
            ProjectService.updateProject(testProject6.id, ProjectDataUpdate(Some("new-name"), None), testOwner1)
          }.isInstanceOf[NoResourceException] shouldBe  true
        }
      }

      "throw not-authorised-exception when somebody tries to update a project that was not created by them" in new Context {
        awaitForResult {
          for {
            _ <- ProjectService.createProject(testProject6)
          } yield intercept[Exception] {
            ProjectService.updateProject(testProject6.id, ProjectDataUpdate(Some("new-name"), None), testOwner2)
          }.isInstanceOf[NotAuthorisedException] shouldBe  true
        }
      }
    }

  }

  trait Context {
    val ProjectStorage = new InMemoryProjectStorage()
    val ProjectValidator = new ProjectValidator(ProjectStorage)
    val ProjectService = new ProjectService(ProjectStorage, ProjectValidator)

    val endPoint: Long = 0L

    val testOwner1: String = "30313317-59c1-4a53-965e-20baf6f2e1f3"
    val testOwner2: String = "c145395f-8f73-44db-a1e1-156300bf6a71"

    val testProject1: Project = testProject(testOwner1)
    val testProject2: Project = testProject(testOwner2)

    val testProject3: Project = testProject(testOwner1)
    val testProject4: Project = testProject(testOwner1)
    val testProject5: Project = testProject(testOwner1)
    val testProject6: Project = testProject(testOwner1)
    def testProject(owner: UserId):Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString,Random.nextLong(1637336456494L), 0L, owner)
  }

}
