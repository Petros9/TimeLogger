package http.routes


import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.server.Route
import core.BaseServiceTest
import core.auth.AuthService

import scala.concurrent.Future

class AuthRouteTest extends BaseServiceTest {

  "AuthRoute" when {

    "POST /auth/signIn" should {

      "return 200 and token if sign in successful" in new Context {
        when(authService.signIn("test")).thenReturn(Future.successful(Some("token")))
        val requestEntity: HttpEntity.Strict = HttpEntity(MediaTypes.`application/json`, s"""{"login": "test"}""")

        Post("/auth/signIn", requestEntity) ~> authRoute ~> check {
          responseAs[String] shouldBe "\"token\""
          status.intValue() shouldBe 200
        }
      }

      "return 400 if signIn unsuccessful" in new Context {
        when(authService.signIn("test")).thenReturn(Future.successful(None))
        val requestEntity: HttpEntity.Strict = HttpEntity(MediaTypes.`application/json`, s"""{"login": "test"}""")

        Post("/auth/signIn", requestEntity) ~> authRoute ~> check {
          status.intValue() shouldBe 400
        }
      }

    }

    "POST /auth/signUp" should {

      "return 201 and token" in new Context {
        when(authService.signUp("test")).thenReturn(Future.successful("token"))
        val requestEntity: HttpEntity.Strict = HttpEntity(MediaTypes.`application/json`, s"""{"username": "test"}""")

        Post("/auth/signUp", requestEntity) ~> authRoute ~> check {
          responseAs[String] shouldBe "\"token\""
          status.intValue() shouldBe 201
        }
      }

    }

  }

  trait Context {
    val authService: AuthService = mock[AuthService]
    val authRoute: Route = new AuthRoute(authService).route
  }

}
