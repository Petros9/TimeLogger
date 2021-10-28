package http.routes

import akka.http.scaladsl.server.Directives.{as, complete, entity, path, pathEndOrSingleSlash, pathPrefix, post}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax.EncoderOps
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import core.tasks.TaskService
import core.{ TaskDataUpdate, Task}
import utils.SecurityDirectives.authenticate

import scala.concurrent.ExecutionContext
class TaskRoute(
                    secretKey: String,
                    taskService: TaskService
                  )(implicit executionContext: ExecutionContext)
  extends FailFastCirceSupport {
  import taskService._

  val route: Route = pathPrefix("tasks") {
    pathEndOrSingleSlash {
      authenticate(secretKey) { userId =>
        put {
          entity(as[Task]) { task =>
            complete(createTask(task).map(_.asJson))
          }
        } ~
          delete {
            entity(as[Id]) { id =>
              complete(deleteTask(id.id, userId).map(_.asJson))
            }
          } ~
          post {
            entity(as[IdAndUpdateTaskData]) { idAndUpdateTaskData =>
              complete(updateTask(idAndUpdateTaskData.id, idAndUpdateTaskData.updateTaskData, userId).map(_.asJson))
            }
          }
      }
    }
  }
  private case class Id(id: String)
  private case class IdAndUpdateTaskData(id: String, updateTaskData: TaskDataUpdate)
}