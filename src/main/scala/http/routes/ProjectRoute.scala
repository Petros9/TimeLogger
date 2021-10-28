package http.routes

import akka.http.scaladsl.server.Directives.{as, complete, entity, path, pathEndOrSingleSlash, pathPrefix, post}
import core.projects.ProjectService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax.EncoderOps
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import core.{Project, ProjectDataUpdate}
import utils.SecurityDirectives.authenticate
import utils.exceptions.{NameOccupiedException, NameOccupiedResponse, NoResourceException, NoResourceResponse, NotAuthorisedException, NotAuthorisedResponse, TimeConflictResponse}

import scala.concurrent.ExecutionContext
class ProjectRoute(
                    secretKey: String,
                    projectService: ProjectService
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
                  case _: NotAuthorisedException => NotAuthorisedResponse().asJson
                })
            }
          } ~
            delete {
              entity(as[Id]) { id =>
                complete(
                  try deleteProject(id.id, userId).map(_.asJson)
                  catch {
                    case _: NotAuthorisedException => NotAuthorisedResponse().asJson
                    case _: NoResourceException => NoResourceResponse().asJson
                    case _: NameOccupiedException => NameOccupiedResponse().asJson
                  }
                )
              }
            } ~
            post {
              entity(as[IdAndUpdateProjectData]) { idAndUpdateProjectData =>
                complete(
                  try updateProject(idAndUpdateProjectData.id, idAndUpdateProjectData.updateProjectData, userId).map(_.asJson)
                  catch {
                    case _: NotAuthorisedException => NotAuthorisedResponse().asJson
                    case _: NoResourceException => NoResourceResponse().asJson
                    case _: NameOccupiedException => NameOccupiedResponse().asJson
                  }
                )
              }
            } ~
            get {
              entity(as[Id]) { id =>
                complete(
                  try getProject(id.id, userId).map(_.asJson)
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
            complete(getProjects(userId).map(_.asJson))
          }
        }
      }
    }
  }
  private case class Id(id: String)
  private case class IdAndUpdateProjectData(id: String, updateProjectData: ProjectDataUpdate)
}
