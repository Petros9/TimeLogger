package http.routes

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import core.{AuthTokenContent, BaseServiceTest, Project, Task, TaskDataUpdate, UserId}
import core.projects.{InMemoryProjectStorage, ProjectService, ProjectValidator}
import core.tasks.{InMemoryTaskStorage, TaskService, TaskValidator}
import io.circe.Encoder.AsArray.importedAsArrayEncoder
import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps
import pdi.jwt.{Jwt, JwtAlgorithm}
import utils.responses._

import java.util.UUID
import scala.concurrent.Future
import scala.util.Random

class TaskRouteTest  extends BaseServiceTest {

  "TaskRoute" when {

    "PUT /tasks" should {
      "return 200 and create task" in new Context {
        //projectService.createProject(testProject1)
        when(taskService.createTask(testTask1)).thenReturn(Future.successful(testTask1))
        val header = RawHeader("Token", buildAuthToken(testOwner1))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testTask1.id}", "projectId": "${testTask1.projectId}", "startPointer": "${testTask1.startPointer}", "volume": "${testTask1.volume}", "workingTime": "${testTask1.workingTime}", "desc": "${testTask1.desc}", "endPointer": "${testTask1.endPointer}"}""")

        Put("/tasks", requestEntity).withHeaders(header) ~> taskRoute ~> check {
          responseAs[String] shouldBe testTask1.asJson.noSpaces
          status.intValue() shouldBe 200
        }
      }

      "return no resource response" in new Context {

        val header = RawHeader("Token", buildAuthToken(testOwner1))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testTask1.id}", "projectId": "${testProject3.id}", "startPointer": "${testTask1.startPointer}", "volume": "${testTask1.volume}", "workingTime": "${testTask1.workingTime}", "desc": "${testTask1.desc}", "endPointer": "${testTask1.endPointer}"}""")

        Put("/tasks", requestEntity).withHeaders(header) ~> taskRoute ~> check {
          responseAs[String] shouldBe NoResourceResponse().asJson.noSpaces
        }
      }
    }

    "DELETE /tasks" should {

      "return 200 and mark task as deleted" in new Context {
        projectService.createProject(testProject1)
        taskService.createTask(testTask2)
        val header = RawHeader("Token", buildAuthToken(testOwner1))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testTask2.id}"}""")

        Delete("/tasks", requestEntity).withHeaders(header) ~> taskRoute ~> check {
          status.intValue() shouldBe 200
        }
      }

      "return not authorised response" in new Context {
        projectService.createProject(testProject1)
        taskService.createTask(testTask2)
        val header = RawHeader("Token", buildAuthToken(testOwner2))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testTask2.id}"}""")

        Delete("/tasks", requestEntity).withHeaders(header) ~> taskRoute ~> check {
          responseAs[String] shouldBe NotAuthorisedResponse().asJson.noSpaces
        }
      }
    }

    "POST /tasks" should {

      "update task and return 200" in new Context {

        projectService.createProject(testProject1)
        taskService.createTask(testTask2)
        val updateTestTask2 = TaskDataUpdate(None, Some(1234), None, Some("asdas"), None)
        val header = RawHeader("Token", buildAuthToken(testOwner1))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testTask2.id}", "startPointer": "${testTask2.startPointer}", "volume": "${testTask2.volume + 13}", "workingTime": "${testTask2.workingTime}", "desc": "${testTask2.desc + "asd"}", "endPointer": "${testTask2.endPointer}"}""")
        Post("/tasks", requestEntity).withHeaders(header) ~> taskRoute ~> check {
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
    val taskRoute: Route = new TaskRoute(secretKey, taskService).route
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
    val testTask2: Task = testTask(UUID.randomUUID().toString, testProject1.id, Random.nextLong(1637336456494L),2, 12L, "desc")
    val testTask3: Task = testTask(UUID.randomUUID().toString, testProject2.id, Random.nextLong(1637336456494L),2, 12L, "desc")
    def testTask(id: String, projectId: String, startPointer: Long, volume: Int, workingTime: Long, desc: String): Task = Task(id, projectId, startPointer, volume, workingTime, desc)
  }
}
