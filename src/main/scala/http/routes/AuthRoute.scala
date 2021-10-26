package http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, pathEndOrSingleSlash, pathPrefix, post}
import core.auth.AuthService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax.EncoderOps
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext

class AuthRoute(authService: AuthService)(implicit executionContext: ExecutionContext) extends FailFastCirceSupport {

  import StatusCodes._
  import authService._

  val route = pathPrefix("auth") {
    path("signIn") {
      pathEndOrSingleSlash {
        post {
          entity(as[Login]) { login =>
            complete(
              signIn(login.login).map {
                case Some(token) => OK         -> token.asJson
                case None        => BadRequest -> None.asJson
              }
            )
          }
        }
      }
    } ~
      path("signUp") {
        pathEndOrSingleSlash {
          post {
            entity(as[Username]) { userEntity =>
              complete(Created -> signUp(userEntity.username))
            }
          }
        }
      }
  }

  private case class Login(login: String)
  private case class Username(username: String)
}
