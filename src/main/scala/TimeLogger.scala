import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import core.auth.{AuthService, JdbcAuthDataStorage}
import http.HttpRoute

import utils.database.{DatabaseConnector, DatabaseMigrationManager, Config}

import scala.concurrent.ExecutionContext

object TimeLogger extends App {

  def startApplication() = {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val executor: ExecutionContext      = actorSystem.dispatcher

    val config = new Config

    /*new DatabaseMigrationManager(
      config.jdbcUrl,
      config.username,
      config.password
    ).migrateDatabaseSchema()*/

    val databaseConnector = new DatabaseConnector(
      config.jdbcUrl,
      config.username,
      config.password
    )

    val authDataStorage    = new JdbcAuthDataStorage(databaseConnector)
    val authService  = new AuthService(authDataStorage, config.secretKey)
    val httpRoute    = new HttpRoute(authService, config.secretKey)

    Http().newServerAt(config.host, config.port).bind(httpRoute.route)
  }

  startApplication()

}
