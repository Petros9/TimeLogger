package http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, pathEndOrSingleSlash, pathPrefix, post}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax.EncoderOps
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import core.tasks.TaskService
import core.{Task, TaskDataUpdate}
import utils.SecurityDirectives.authenticate
import utils.responses.{NoResourceException, NoResourceResponse, NotAuthorisedException, NotAuthorisedResponse, TimeConflictException, TimeConflictResponse}

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
            complete(
              try createTask(task).map(_.asJson)
              catch {
                case _: NoResourceException => (StatusCodes.NoContent, NoResourceResponse().asJson)
                case _: TimeConflictException => (StatusCodes.Conflict, TimeConflictResponse().asJson)
              })
          }
        } ~
          delete {
            entity(as[Id]) { id =>
              complete(
                try deleteTask(id.id, userId).map(_.asJson)
                catch {
                  case _: NoResourceException => (StatusCodes.NoContent, NoResourceResponse().asJson)
                  case _: TimeConflictException => (StatusCodes.Conflict, TimeConflictResponse().asJson)
                  case _: NotAuthorisedException => (StatusCodes.Unauthorized, NotAuthorisedResponse().asJson)
                })
            }
          } ~
          post {
            entity(as[IdAndUpdateTaskData]) { idAndUpdateTaskData =>
              complete(
                try {
                  updateTask(idAndUpdateTaskData.id, TaskDataUpdate(Option(idAndUpdateTaskData.startPointer), Option(idAndUpdateTaskData.volume), Option(idAndUpdateTaskData.workingTime), Option(idAndUpdateTaskData.desc), Option(idAndUpdateTaskData.endPointer)), userId).map(_.asJson)
                }
            catch {
                  case _: NoResourceException => (StatusCodes.NoContent, NoResourceResponse().asJson)
                  case _: TimeConflictException => (StatusCodes.Conflict, TimeConflictResponse().asJson)
                  case _: NotAuthorisedException => (StatusCodes.Unauthorized, NotAuthorisedResponse().asJson)
                })
            }
          }
      }
    }
  }
  private case class Id(id: String)
  private case class IdAndUpdateTaskData(id: String, startPointer: Long, volume: Int, workingTime: Long, desc: String, endPointer: Long)
}