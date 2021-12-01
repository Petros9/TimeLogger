package http.routes

import akka.http.scaladsl.server.Directives.{as, complete, entity, path, pathEndOrSingleSlash, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import core.projects.ProjectService
import core.tasks.TaskService
import core.{Project, ProjectDataUpdate, ProjectsFilters}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax.EncoderOps
import utils.SecurityDirectives.authenticate
import utils.responses.{NameOccupiedException, _}

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
            get {
              entity(as[Id]) { id =>
                complete(
                  try getProject(id.id, userId).map( project =>
                    {
                      try {
                        val tasks = taskService.getProjectTasks(project.get.id, userId)
                        tasks.isEmpty match {
                          case true => ProjectInfoResponse(project.get, Seq(), 0).asJson
                          case false => ProjectInfoResponse(project.get, tasks, tasks.map(_.workingTime).sum).asJson
                        }
                      } catch {
                        case _: NullPointerException => project.get.asJson
                      }
                    }
                  )
                  catch {
                    case _: NotAuthorisedException => NotAuthorisedResponse().asJson
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
            entity(as[ProjectsFilters]) { projectFilters =>
              complete(ProjectsInfoResponse( getProjects(userId, projectFilters), projectFilters.updateTimeIncSorting, taskService).asJson)
            }
          }
        }
      }
    }
  }
  private case class Id(id: String)
  private case class IdAndUpdateProjectData(id: String, updateProjectData: ProjectDataUpdate)
}
