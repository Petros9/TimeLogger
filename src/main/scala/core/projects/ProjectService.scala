package core.projects

import core.{Project, ProjectDataUpdate, ProjectId, ProjectsFilters}
import utils.MonadTransformers.FutureOptionMonadTransformer
import utils.responses.{NameOccupiedException, NoResourceException, NotAuthorisedException}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class ProjectService(projectDataStorage: ProjectDataStorage, projectValidator: ProjectValidator)
  (implicit executionContext: ExecutionContext) {

  def deleteProject(id: String, token: String): Future[Option[Project]]=
    updateProject(id, ProjectDataUpdate(None, Option(System.currentTimeMillis())), token)

  def idListFilter(projectId: ProjectId, projectIdListOption: Option[Seq[ProjectId]]): Boolean = {
        projectIdListOption match {
          case Some(projectIdList) => projectIdList.contains(projectId)
          case None => true
        }
  }

  def startTimeFilter(projectStartTime: Long, startTimeOption: Option[Long]): Boolean = {
    startTimeOption match {
      case Some(startTime) => startTime >= projectStartTime
      case None => true
    }
  }

  def endTimeFilter(projectEndTime: Long, endTimeOption: Option[Long]): Boolean = {
    endTimeOption match {
      case Some(endTime) => projectEndTime <= endTime
      case None => true
    }
  }

  def deleted(projectEndTime: Long, deletedOption: Option[Boolean]): Boolean = {
    deletedOption match {
      case Some(deleted) => {
        val isProjectDeleted = projectEndTime == 0L
        deleted == isProjectDeleted
      }
      case None => true
    }
  }

  def sortByStartTime(projects: Seq[Project], creationTimeSortingOption: Option[Boolean]): Seq[Project] = {
    creationTimeSortingOption match {
      case Some(creationTimeSortingOption) =>
        if(creationTimeSortingOption) projects.sortBy(_.startPointer)
        else projects.sortBy(_.startPointer).reverse

      case None => projects
    }
  }

  def getProjects(token: String, projectFilters: ProjectsFilters): Seq[Project] = {
    val allProjects = Await.result(projectDataStorage.getProjects(token), Duration.Inf)
    val filteredProjects = allProjects.filter(project =>
      idListFilter(project.id, projectFilters.idList) &&
        startTimeFilter(project.startPointer, projectFilters.startTime) &&
          endTimeFilter(project.endPointer, projectFilters.endTime))
    filteredProjects.sortBy(_.startPointer)
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
    } else if (!projectValidator.nameIsFreeToBeUsed(projectUpdate.projectName)){
      throw new NameOccupiedException
    } else {
      projectDataStorage
        .getProject(id)
        .mapT(projectUpdate.merge)
        .flatMapTOuter(projectDataStorage.saveProject)
    }
  }

}