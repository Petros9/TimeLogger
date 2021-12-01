package http.routes


import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import core.{AuthTokenContent, BaseServiceTest, Project, ProjectDataUpdate, Task, UserId}
import core.projects.{InMemoryProjectStorage, ProjectService, ProjectValidator}
import core.tasks.{InMemoryTaskStorage, TaskService, TaskValidator}
import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps
import pdi.jwt.{Jwt, JwtAlgorithm}
import utils.responses._

import java.util.UUID

import scala.util.Random

class ProjectRouteTest extends BaseServiceTest {

  "ProjectRoute" when {

    "PUT /projects/single_project" should {

      "return 200 and create project" in new Context {
        val header = RawHeader("Token", buildAuthToken(testOwner1))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testProject1.id}", "projectName": "${testProject1.projectName}", "startPointer": "${testProject1.startPointer}", "endPointer": "${testProject1.endPointer}", "owner": "${testProject1.owner}"}""")
        Put("/projects/single_project", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          responseAs[String] shouldBe testProject1.asJson.noSpaces
          status.intValue() shouldBe 200
        }
      }

      "return name occupied response" in new Context {
        projectService.createProject(testProject1)
        val header = RawHeader("Token", buildAuthToken(testOwner1))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testProject2.id}", "projectName": "${testProject1.projectName}", "startPointer": "${testProject1.startPointer}", "endPointer": "${testProject1.endPointer}", "owner": "${testProject1.owner}"}""")
        Put("/projects/single_project", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          responseAs[String] shouldBe NameOccupiedResponse.asJson.noSpaces
        }
      }
    }

    "DELETE /projects/single_project" should {

      "return 200 and mark project as deleted" in new Context {
        projectService.createProject(testProject1)
        val header = RawHeader("Token", buildAuthToken(testOwner1))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testProject1.id}"}""")

        Delete("/projects/single_project", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          status.intValue() shouldBe 200
        }
      }

      "return not authorised response" in new Context {
        projectService.createProject(testProject1)
        val header = RawHeader("Token", buildAuthToken(testOwner2))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testProject1.id}"}""")

        Delete("/projects/single_project", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          responseAs[String] shouldBe NotAuthorisedResponse().asJson.noSpaces
        }
      }
    }

    "POST /projects/single_project" should {

      "update project and return 200" in new Context {

        projectService.createProject(testProject1)
        val updateProject = ProjectDataUpdate(Some("new-project-name"), Some(testProject1.endPointer))
        val header = RawHeader("Token", buildAuthToken(testOwner1))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testProject1.id}", "projectName": "${updateProject.projectName.orNull}", "endPoint": "${testProject1.endPointer}"}""")
        Post("/projects/single_project", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          status.intValue() shouldBe 200
        }
      }

      "return no authorised response" in new Context {
        projectService.createProject(testProject1)
        val updateProject = ProjectDataUpdate(Some("new-project-name"), Some(testProject1.endPointer))
        val header = RawHeader("Token", buildAuthToken(testOwner2))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testProject1.id}", "projectName": "${updateProject.projectName.orNull}", "endPoint": "${testProject1.endPointer}"}""")
        Post("/projects/single_project", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          responseAs[String] shouldBe NotAuthorisedResponse().asJson.noSpaces
        }
      }

    }

    "GET /projects/single_project" should {

      "provide project with no tasks and return 200" in new Context {
        projectService.createProject(testProject1)

        val header = RawHeader("Token", buildAuthToken(testOwner1))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testProject1.id}"}""")
        Get("/projects/single_project", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          responseAs[String] shouldBe ProjectInfoResponse(testProject1, Seq(), 0).asJson.noSpaces
          status.intValue() shouldBe 200
        }
      }

      "provide project with tasks and return 200" in new Context {
        projectService.createProject(testProject1)
        taskService.createTask(testTask1)
        val header = RawHeader("Token", buildAuthToken(testOwner1))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testProject1.id}"}""")
        Get("/projects/single_project", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          responseAs[String] shouldBe ProjectInfoResponse(testProject1, Seq(testTask1), testTask1.workingTime).asJson.noSpaces
          status.intValue() shouldBe 200
        }
      }
    }

    "GET /projects/all_projects" should {

      "return 200 and added user projects with tasks" in new Context {
        projectService.createProject(testProject1)
        projectService.createProject(testProject3)
        projectService.createProject(testProject4)
        taskService.createTask(testTask1)
        val header = RawHeader("Token", buildAuthToken(testOwner1))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"idList": ${Seq(testProject1.id, testProject3.id, testProject4.id).asJson},"startTime": "${0L}", "endTime": "${Long.MaxValue}", "deleted": ${false}, "creationTimeIncSorting": ${true}, "updateTimeIncSorting": ${true}}""")
        Get("/projects/all_projects", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          responseAs[String] shouldBe ProjectsInfoResponse(Seq(testProject1, testProject4, testProject3), Some(true), taskService).asJson.noSpaces
          status.intValue() shouldBe 200
        }
      }
    }
  }

  trait Context {
    val secretKey = "secret"
    val TaskStorage = new InMemoryTaskStorage()
    val ProjectStorage = new InMemoryProjectStorage()
    val ProjectValidator = new ProjectValidator(ProjectStorage)
    val TaskValidator = new TaskValidator(TaskStorage, ProjectValidator)
    val taskService = new TaskService(TaskStorage, TaskValidator, ProjectValidator)
    val projectService = new ProjectService(ProjectStorage, ProjectValidator)
    val projectRoute: Route = new ProjectRoute(secretKey, projectService, taskService).route
    val testOwner1: String = "30313317-59c1-4a53-965e-20baf6f2e1f3"
    val testOwner2: String = "c145395f-8f73-44db-a1e1-156300bf6a71"

    val testProject1: Project = testProject(testOwner1)
    val testProject2: Project = testProject(testOwner2)

    val testProject3: Project = testProject(testOwner1)
    val testProject4: Project = testProject(testOwner1)
    val testProject5: Project = testProject(testOwner1)
    val testProject6: Project = testProject(testOwner1)
    def testProject(owner: UserId):Project = Project(UUID.randomUUID().toString, UUID.randomUUID().toString,Random.nextLong(1637336456494L), 0L, owner)

    def buildAuthToken(id: String): String = Jwt.encode(AuthTokenContent(id).asJson.noSpaces, secretKey, JwtAlgorithm.HS256)

    val testTask1: Task = testTask(UUID.randomUUID().toString, testProject1.id, Random.nextLong(1637336456494L),2, 12L, "desc")
    def testTask(id: String, projectId: String, startPointer: Long, volume: Int, workingTime: Long, desc: String): Task = Task(id, projectId, startPointer, volume, workingTime, desc)}

}
