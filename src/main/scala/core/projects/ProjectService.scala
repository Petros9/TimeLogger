package core.projects

import core.{Project, ProjectDataUpdate}
import utils.MonadTransformers.FutureOptionMonadTransformer

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ProjectService(projectDataStorage: ProjectDataStorage, projectValidator: ProjectValidator)
  (implicit executionContext: ExecutionContext) {

  def deleteProject(id: String, token: String): Future[Option[Project]]=
    updateProject(id, ProjectDataUpdate(None, Option(System.currentTimeMillis())), token)

  def getProjects(token: String): Future[Seq[Project]] =
    projectDataStorage.getProjects()

  def getProject(id: String, token: String): Future[Option[Project]] = {
    if(projectValidator.isOwner(id, token)){
      projectDataStorage.getProject(id)
    }
  }

  def createProject(project: Project): Future[Project] = {
    if(projectValidator.nameIsFreeToBeUsed(Option(project.projectName))){
      projectDataStorage.saveProject(project)
    }
  }

  def updateProject(id: String, projectUpdate: ProjectDataUpdate, token: String): Future[Option[Project]] = {
    if(projectValidator.isOwner(id, token) && projectValidator.isNotDeleted(id) && projectValidator.nameIsFreeToBeUsed(projectUpdate.projectName))
    projectDataStorage
      .getProject(id)
      .mapT(projectUpdate.merge)
      .flatMapTOuter(projectDataStorage.saveProject)
  }

}