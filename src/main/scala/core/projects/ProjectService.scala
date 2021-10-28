package core.projects

import core.{Project, ProjectDataUpdate}
import utils.MonadTransformers.FutureOptionMonadTransformer
import utils.exceptions.{NameOccupiedException, NoResourceException, NotAuthorisedException}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ProjectService(projectDataStorage: ProjectDataStorage, projectValidator: ProjectValidator)
  (implicit executionContext: ExecutionContext) {

  def deleteProject(id: String, token: String): Future[Option[Project]]=
    updateProject(id, ProjectDataUpdate(None, Option(System.currentTimeMillis())), token)

  def getProjects(token: String): Future[Seq[Project]] = {
    projectDataStorage.getProjects(token)
  }

  @throws(classOf[NotAuthorisedException])
  def getProject(id: String, token: String): Future[Option[Project]] = {
    if(projectValidator.isOwner(id, token)){
      projectDataStorage.getProject(id)
    } else {
      throw new NotAuthorisedException
    }
  }

  @throws(classOf[NameOccupiedException])
  def createProject(project: Project): Future[Project] = {
    if(projectValidator.nameIsFreeToBeUsed(Option(project.projectName))){
      projectDataStorage.saveProject(project)
    } else {
      throw new NameOccupiedException
    }
  }

  @throws(classOf[NotAuthorisedException])
  @throws(classOf[NoResourceException])
  @throws(classOf[NameOccupiedException])
  def updateProject(id: String, projectUpdate: ProjectDataUpdate, token: String): Future[Option[Project]] = {
    if(!projectValidator.isOwner(id, token)){
      throw new NotAuthorisedException
    } else if(!projectValidator.isNotDeleted(id)){
      throw new NoResourceException
    } else if (projectValidator.nameIsFreeToBeUsed(projectUpdate.projectName)){
      throw new NameOccupiedException
    } else {
      projectDataStorage
        .getProject(id)
        .mapT(projectUpdate.merge)
        .flatMapTOuter(projectDataStorage.saveProject)
    }
  }

}