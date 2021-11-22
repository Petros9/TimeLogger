package http

import akka.http.scaladsl.server.Route
import core.BaseServiceTest
import core.auth.AuthService
import core.projects.ProjectService
import core.tasks.TaskService

class HttpRouteTest extends BaseServiceTest {

  "HttpRoute" when {

    "GET /healthcheck" should {

      "return 200 OK" in new Context {
        Get("/healthcheck") ~> httpRoute ~> check {
          responseAs[String] shouldBe "OK"
          status.intValue() shouldBe 200
        }
      }

    }

  }

  trait Context {
    val secretKey = "secret"
    val taskService: TaskService = mock[TaskService]
    val projectService: ProjectService = mock[ProjectService]
    val authService: AuthService = mock[AuthService]

    val httpRoute: Route = new HttpRoute(taskService, projectService, authService, secretKey).route
  }

}
