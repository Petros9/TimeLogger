package core.projects

import core.{Project, ProjectDataUpdate}
import utils.MonadTransformers.FutureOptionMonadTransformer

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ProjectService(projectDataStorage: ProjectDataStorage)
  (implicit executionContext: ExecutionContext) {

  def getProjects(): Future[Seq[Project]] =
  projectDataStorage.getProjects()

  def getProject(id: String): Future[Option[Project]] =
  projectDataStorage.getProject(id)

  def createProject(project: Project): Future[Project] = {
    projectDataStorage.saveProject(project)
  }

  def updateProject(id: String, projectUpdate: ProjectDataUpdate): Future[Option[Project]] = {
    val projects =
    projectDataStorage
      .getProject(id)
      .mapT(projectUpdate.merge)
      .flatMapTOuter(projectDataStorage.saveProject)
  }

  }