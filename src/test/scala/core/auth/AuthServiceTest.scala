package core.auth

import core.{AuthData, BaseServiceTest}
import pdi.jwt.{Jwt, JwtAlgorithm}

import java.util.UUID
import scala.util.Random

class AuthServiceTest extends BaseServiceTest {

  "AuthServiceTest" when {

    "signIn" should {

      "return valid auth token" in new Context {
        awaitForResult(for {
          _ <- authDataStorage.saveAuthData(testAuthData)
          Some(token) <- authService.signIn(testUsername)
        } yield Jwt.decodeRaw(token, secretKey, Seq(JwtAlgorithm.HS256)).isSuccess shouldBe true)
      }

      "return None if user not exists" in new Context {
        awaitForResult(for {
          result <- authService.signIn(testUsername)
        } yield result shouldBe None)
      }

    }

    "signUp" should {

      "return valid auth token" in new Context {
        awaitForResult(for {
          token <- authService.signUp(testUsername)
        } yield Jwt.decodeRaw(token, secretKey, Seq(JwtAlgorithm.HS256)).isSuccess shouldBe true)
      }

      "store auth data with encrypted password in database" in new Context {
        awaitForResult(for {
          _ <- authService.signUp(testUsername)
          Some(authData) <- authDataStorage.findAuthData(testUsername)
        } yield {
          authData.username shouldBe testUsername
        })
      }

    }

  }

  trait Context {
    val secretKey = "secret"
    val authDataStorage = new InMemoryAuthDataStorage
    val authService = new AuthService(authDataStorage, secretKey)

    val testId: String = UUID.randomUUID().toString
    val testUsername: String = Random.nextString(10)

    val testAuthData: AuthData = AuthData(testId, testUsername)
  }

}