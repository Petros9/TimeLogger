package http

import akka.http.scaladsl.server.Directives.{complete, get, pathPrefix}
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import core.auth.AuthService
import http.routes.{AuthRoute, ProjectRoute}
import akka.http.scaladsl.server.Directives._
import core.projects.ProjectService
import core.tasks.TaskService

import scala.concurrent.ExecutionContext

class HttpRoute(
                 taskService: TaskService,
                 projectService: ProjectService,
                 authService: AuthService,
                 secretKey: String
               )(implicit executionContext: ExecutionContext) {

  private val authRouter  = new AuthRoute(authService)
  private val projectRouter = new ProjectRoute(secretKey, projectService)

  val route: Route =
    cors() {
      pathPrefix("v1") {
          authRouter.route
      } ~
      pathPrefix("healthcheck") {
          get {
            complete("OK")
          }
      } ~
        pathPrefix("healthcheck") {
          get {
            complete("OK")
          }
        }
    }

}
