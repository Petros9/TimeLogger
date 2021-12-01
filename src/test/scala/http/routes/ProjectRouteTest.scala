package http.routes


import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import core.{AuthTokenContent, BaseServiceTest, Project, UserId}
import core.projects.ProjectService
import core.tasks.TaskService
import io.circe.Encoder.AsArray.importedAsArrayEncoder
import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps
import pdi.jwt.{Jwt, JwtAlgorithm}
import utils.responses._

import java.util.UUID
import scala.concurrent.Future
import scala.util.Random

class ProjectRouteTest extends BaseServiceTest {

  "ProjectRoute" when {

    "PUT /projects/single_project" should {

      /*"return 200 and create project" in new Context {
        when(projectService.createProject(testProject1)).thenReturn(Future.successful(testProject1))
        val header: RawHeader = RawHeader("token", testOwner1)
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "", "projectName": "", "startPointer": "", "projectName": "", "endPointer": "", "owner": ""}""")
        Put("/projects/single_project", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          responseAs[String] shouldBe testProject1.asJson.noSpaces
          status.intValue() shouldBe 200
        }
      }*/

      /*"return 400 and throw name occupied exception" in new Context {
        val addedProject: Project = Project(testProject1.id, testProject1.projectName, testProject1.startPointer, testProject1.endPointer, testOwner1)
        projectService.createProject(addedProject)
        when(projectService.createProject(testProject1)).thenReturn(Future.successful(NameOccupiedResponse))

        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"firstName": "", "lastName": ""}""")

        Post("/profiles/me", requestEntity).withHeaders(header) ~> profileRoute ~> check {
          responseAs[String] shouldBe testProfile1.asJson.noSpaces
          status.intValue() shouldBe 200
        }
      }*/
    }

    /*"DELETE /projects/single_project" should {

      "return 200 and mark project as deleted" in new Context {
        when(userProfileService.getProfile(testProfile1.id)).thenReturn(Future.successful(Some(testProfile1)))
        val header = RawHeader("Token", buildAuthToken(testProfile1.id))

        Get("/profiles/me").withHeaders(header) ~> projectRoute ~> check {
          responseAs[String] shouldBe testProfile1.asJson.noSpaces
          status.intValue() shouldBe 200
        }
      }

      "return 500 if token is incorrect" in new Context {
        val header = RawHeader("Token", "token")

        Get("/profiles/me").withHeaders(header) ~> projectRoute ~> check {
          status.intValue() shouldBe 500
        }
      }

      "return 500 if token no resource is available" in new Context {
        val header = RawHeader("Token", "token")

        Get("/profiles/me").withHeaders(header) ~> projectRoute ~> check {
          status.intValue() shouldBe 500
        }
      }
    }

    "POST /projects/single_project" should {

      "update project and return 200" in new Context {
        when(userProfileService.updateProfile(testProfile1.id, UserProfileUpdate(Some(""), Some("")))).thenReturn(Future.successful(Some(testProfile1)))
        val header = RawHeader("Token", buildAuthToken(testProfile1.id))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"firstName": "", "lastName": ""}""")

        Post("/profiles/me", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          responseAs[String] shouldBe testProfile1.asJson.noSpaces
          status.intValue() shouldBe 200
        }
      }

      "return 500 if token is incorrect" in new Context {
        val header = RawHeader("Token", "token")
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"firstName": "", "lastName": ""}""")

        Post("/profiles/me", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          status.intValue() shouldBe 500
        }
      }
      "return 500 if token is resource does not exist" in new Context {
        val header = RawHeader("Token", "token")
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"firstName": "", "lastName": ""}""")

        Post("/profiles/me", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          status.intValue() shouldBe 500
        }
      }
      "return 500 if new project name is occupied" in new Context {
        val header = RawHeader("Token", "token")
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"firstName": "", "lastName": ""}""")

        Post("/profiles/me", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          status.intValue() shouldBe 500
        }
      }

    }*/
    "GET /projects/single_project" should {

      "provide project with tasks and return 200" in new Context {
        when(projectService.getProject(testProject1.id, testOwner1)).thenReturn(Future.successful(Some(testProject1)))
        val header = RawHeader("Token", buildAuthToken(testOwner1))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"id": "${testProject1.id}"}""")
        Get("/projects/single_project", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          responseAs[String] shouldBe testProject1.asJson.noSpaces
          status.intValue() shouldBe 200
        }
      }

      /*"return 500 if token is incorrect" in new Context {
        val header = RawHeader("Token", "token")
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"firstName": "", "lastName": ""}""")

        Post("/profiles/me", requestEntity).withHeaders(header) ~> projectRoute ~> check {
          status.intValue() shouldBe 500
        }
      }*/
    }

    /*"GET /profiles/:id" should {

      "return 200 and user profile JSON" in new Context {
        when(userProfileService.getProfile(testProfile1.id)).thenReturn(Future.successful(Some(testProfile1)))

        Get("/profiles/" + testProfileId1) ~> projectRoute ~> check {
          responseAs[String] shouldBe testProfile1.asJson.noSpaces
          status.intValue() shouldBe 200
        }
      }

      "return 400 if profile not exists" in new Context {
        when(userProfileService.getProfile(testProfile1.id)).thenReturn(Future.successful(None))

        Get("/profiles/" + testProfileId1) ~> projectRoute ~> check {
          status.intValue() shouldBe 400
        }
      }

    }

    "POST /projects/all_projects" should {

      "return all users project and return 200" in new Context {
        when(userProfileService.updateProfile(testProfile1.id, UserProfileUpdate(Some(""), Some("")))).thenReturn(Future.successful(Some(testProfile1)))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"firstName": "", "lastName": ""}""")

        Post("/profiles/" + testProfileId1, requestEntity) ~> projectRoute ~> check {
          responseAs[String] shouldBe testProfile1.asJson.noSpaces
          status.intValue() shouldBe 200
        }
      }

      "return 200 and all users project sorted" in new Context {
        when(userProfileService.updateProfile(testProfile1.id, UserProfileUpdate(Some(""), Some("")))).thenReturn(Future.successful(None))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"firstName": "", "lastName": ""}""")

        Post("/profiles/" + testProfileId1, requestEntity) ~> projectRoute ~> check {
          status.intValue() shouldBe 400
        }
      }

    }*/

  }

  trait Context {
    val secretKey = "secret"
    val projectService: ProjectService = mock[ProjectService]
    val taskService: TaskService = mock[TaskService]
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
  }

}
