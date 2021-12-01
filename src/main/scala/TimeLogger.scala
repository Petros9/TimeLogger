import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import core.auth.{AuthService, JdbcAuthDataStorage}
import core.projects.{JdbcProjectDataStorage, ProjectService, ProjectValidator}
import core.tasks.{JdbcTaskDataStorage, TaskService, TaskValidator}
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
    val projectValidator = new ProjectValidator(projectDataStorage)
    val projectService = new ProjectService(projectDataStorage, projectValidator)

    val taskDataStorage = new JdbcTaskDataStorage(databaseConnector)
    val taskValidator = new TaskValidator(taskDataStorage, projectValidator)
    val taskService = new TaskService(taskDataStorage, taskValidator, projectValidator)

    val httpRoute    = new HttpRoute(taskService, projectService, authService, config.secretKey)

    Http().newServerAt(config.host, config.port).bind(httpRoute.route)
  }

  startApplication()

}
