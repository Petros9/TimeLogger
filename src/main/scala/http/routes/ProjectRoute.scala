package http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, pathEndOrSingleSlash, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import core.projects.ProjectService
import core.tasks.TaskService
import core.{Project, ProjectDataUpdate, ProjectId, ProjectsFilters}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax.EncoderOps
import utils.SecurityDirectives.authenticate
import utils.responses._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
class ProjectRoute(
                    secretKey: String,
                    projectService: ProjectService,
                    taskService: TaskService
                  )(implicit executionContext: ExecutionContext)
  extends FailFastCirceSupport {
  import projectService._

  val route: Route = pathPrefix("projects") {
    path("single_project") {
      pathEndOrSingleSlash {
        authenticate(secretKey) { userId =>
          put {
            entity(as[Project]) { project =>
              complete(
                try createProject(project).map(_.asJson)
                catch  {
                  case _: NameOccupiedException => (StatusCodes.Conflict, NameOccupiedResponse().asJson)
                })
            }
          } ~
            delete {
              entity(as[Id]) { id =>
                complete(
                  try deleteProject(id.id, userId).map(_.asJson)
                  catch {
                    case _: NotAuthorisedException => (StatusCodes.Unauthorized, NotAuthorisedResponse().asJson)
                    case _: NoResourceException => (StatusCodes.NoContent, NoResourceResponse().asJson)
                    case _: NameOccupiedException => (StatusCodes.Conflict, NameOccupiedResponse().asJson)
                  }
                )
              }
            } ~
            post {
              entity(as[IdAndUpdateProjectData]) { idAndUpdateProjectData =>
                complete(
                  try updateProject(idAndUpdateProjectData.id, ProjectDataUpdate(Option(idAndUpdateProjectData.projectName), Option(idAndUpdateProjectData.endPoint)), userId).map(_.asJson)
                  catch {
                    case _: NotAuthorisedException => (StatusCodes.Unauthorized, NotAuthorisedResponse().asJson)
                    case _: NoResourceException => (StatusCodes.NoContent, NoResourceResponse().asJson)
                    case _: NameOccupiedException => (StatusCodes.Conflict, NameOccupiedResponse().asJson)
                  }
                )
              }
            } ~
            get {
              entity(as[Id]) { id =>
                complete(
                  try getProject(id.id, userId).map( project => {
                      val tasks = taskService.getProjectTasks(project.get.id, userId)
                      if (tasks.isEmpty) ProjectInfoResponse(project.get, Seq(), 0).asJson
                      else ProjectInfoResponse(project.get, tasks, tasks.map(_.workingTime).sum).asJson
                  })
                  catch {
                    case _: NotAuthorisedException => NotAuthorisedResponse().asJson
                    case _: NoResourceException => NoResourceResponse().asJson
                  }
                )
              }
            }
        }
      }
    } ~
      path("all_projects") {
        pathEndOrSingleSlash {
          authenticate(secretKey) { userId =>
            get {
              entity(as[ProjectsFiltersRoute]) { projectFilters =>
                complete(ProjectsInfoResponse(getProjects(userId, ProjectsFilters(Option(projectFilters.idList), Option(projectFilters.startTime), Option(projectFilters.endTime), Option(projectFilters.deleted), Option(projectFilters.creationTimeIncSorting), Option(projectFilters.updateTimeIncSorting))), Option(projectFilters.updateTimeIncSorting), taskService).asJson)
              }
            }
          }
        }
      }
  }
  private case class Id(id: String)
  private case class IdAndUpdateProjectData(id: String, projectName: String, endPoint: Long)
  private case class ProjectsFiltersRoute(idList: Seq[ProjectId], startTime: Long, endTime: Long, deleted: Boolean, creationTimeIncSorting: Boolean, updateTimeIncSorting: Boolean)
}