package core.auth

import core.{AuthData, BaseServiceTest}
import utils.InMemoryPostgresStorage

import java.util.UUID
import scala.util.Random

class JdbcAuthDataStorageTest extends AuthDataStorageSpec {
  override def authDataStorageBuilder(): AuthDataStorage =
    new JdbcAuthDataStorage(InMemoryPostgresStorage.databaseConnector)
}

class InMemoryAuthDataStorageTest extends AuthDataStorageSpec {
  override def authDataStorageBuilder(): AuthDataStorage =
    new InMemoryAuthDataStorage()
}

abstract class AuthDataStorageSpec extends BaseServiceTest {

  def authDataStorageBuilder(): AuthDataStorage

  "AuthDataStorage" when {

    "saveAuthData" should {

      "return saved auth data" in new Context {
        awaitForResult(for {
          authData <- authDataStorage.saveAuthData(testAuthData)
        } yield authData shouldBe testAuthData)
      }

      "override already saved data" in new Context {
        awaitForResult(for {
          _ <- authDataStorage.saveAuthData(testAuthData.copy(username = "123"))
          authData <- authDataStorage.saveAuthData(testAuthData)
        } yield authData shouldBe testAuthData)
      }

    }

    "findAuthData" should {

      "return auth data where username or email equals to login" in new Context {
        awaitForResult(for {
          _ <- authDataStorage.saveAuthData(testAuthData)
          maybeAuthDataUsername <- authDataStorage.findAuthData(testAuthData.username)
        } yield {
          maybeAuthDataUsername shouldBe Some(testAuthData)
        })
      }

      "return None if user with such login don't exists" in new Context {
        awaitForResult(for {
          maybeAuthData <- authDataStorage.findAuthData(testAuthData.username)
        } yield maybeAuthData shouldBe None)
      }

    }

  }

  trait Context {
    val authDataStorage: AuthDataStorage = authDataStorageBuilder()
    val testAuthData = AuthData(UUID.randomUUID().toString, Random.nextString(10))
  }

}
