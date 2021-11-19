package core.auth

import core.{AuthData, AuthToken, AuthTokenContent, UserId}
import io.circe.Encoder.AsRoot.importedAsRootEncoder
import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps
import pdi.jwt.{Jwt, JwtAlgorithm}
import utils.MonadTransformers.FutureOptionMonadTransformer

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AuthService(
                   authDataStorage: AuthDataStorage,
                   secretKey: String
                 )(implicit executionContext: ExecutionContext) {

  def signIn(login: String): Future[Option[AuthToken]] =
    authDataStorage
      .findAuthData(login)
      .mapT(authData => encodeToken(authData.id))

  def signUp(login: String): Future[AuthToken] =
    authDataStorage
      .saveAuthData(AuthData(UUID.randomUUID().toString, login))
      .map(authData => encodeToken(authData.id))

  def encodeToken(userId: UserId): AuthToken =
    Jwt.encode(AuthTokenContent(userId).asJson.noSpaces, secretKey, JwtAlgorithm.HS256)

}
