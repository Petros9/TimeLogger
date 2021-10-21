import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import core.auth.{AuthService, JdbcAuthDataStorage}
import http.HttpRoute
import utils.Config
import utils.database.{DatabaseConnector, DatabaseMigrationManager}

import scala.concurrent.ExecutionContext

object TimeLogger extends App {

  def startApplication() = {
    implicit val actorSystem                     = ActorSystem()
    implicit val executor: ExecutionContext      = actorSystem.dispatcher

    val config = Config.load()

    new DatabaseMigrationManager(
      config.database.jdbcUrl,
      config.database.username,
      config.database.password
    ).migrateDatabaseSchema()

    val databaseConnector = new DatabaseConnector(
      config.database.jdbcUrl,
      config.database.username,
      config.database.password
    )
    val authDataStorage    = new JdbcAuthDataStorage(databaseConnector)
    val authService  = new AuthService(authDataStorage, config.secretKey)
    val httpRoute    = new HttpRoute(authService, config.secretKey)

    Http().newServerAt(config.http.host, config.http.port).bind(httpRoute.route)
  }

  startApplication()

}
