import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import core.auth.{AuthService, JdbcAuthDataStorage}
import core.projects.{JdbcProjectDataStorage, ProjectDataStorage, ProjectService}
import core.tasks.{JdbcTaskDataStorage, TaskService}
import http.HttpRoute
import utils.database.{Config, DatabaseConnector}

import scala.concurrent.ExecutionContext

object TimeLogger extends App {

  def startApplication() = {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val executor: ExecutionContext      = actorSystem.dispatcher

    val config = new Config

    val databaseConnector = new DatabaseConnector(
      config.jdbcUrl,
      config.username,
      config.password
    )

    val authDataStorage    = new JdbcAuthDataStorage(databaseConnector)
    val authService  = new AuthService(authDataStorage, config.secretKey)

    val projectDataStorage = new JdbcProjectDataStorage(databaseConnector)
    val projectService = new ProjectService(projectDataStorage)

    val taskDataStorage = new JdbcTaskDataStorage(databaseConnector)
    val taskService = new TaskService(taskDataStorage)

    val httpRoute    = new HttpRoute(taskService, projectService, authService, config.secretKey)

    Http().newServerAt(config.host, config.port).bind(httpRoute.route)
  }

  startApplication()

}
