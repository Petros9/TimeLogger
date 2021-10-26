package http

import akka.http.scaladsl.server.Directives.{complete, get, pathPrefix}
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import core.auth.AuthService
import http.routes.AuthRoute
import akka.http.scaladsl.server.Directives._
import scala.concurrent.ExecutionContext

class HttpRoute(
                 authService: AuthService,
                 secretKey: String
               )(implicit executionContext: ExecutionContext) {

  private val authRouter  = new AuthRoute(authService)

  val route: Route =
    cors() {
      pathPrefix("v1") {
        println("hey")
          authRouter.route
      } ~
      pathPrefix("healthcheck") {
          get {
            complete("OK")
          }
      }
    }

}
