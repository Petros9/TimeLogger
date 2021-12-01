package utils.responses

import core.tasks.TaskService
import core.{Project, ProjectsFilters}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ProjectsInfoResponse {

  def findLastUpdate(projectsWithTasks: ProjectInfoResponseWithoutTime): Long = {
    if(projectsWithTasks.tasks.isEmpty) projectsWithTasks.project.startPointer
    else projectsWithTasks.tasks.maxBy(_.startPointer).startPointer
  }

  def apply(projects: Seq[Project], updateTimeSortingOption: Option[Boolean], taskService: TaskService): Seq[ProjectInfoResponseWithoutTime] = {
      val projectsWithTasks = projects.map(project => ProjectInfoResponseWithoutTime(project, taskService.getProjectTasks(project.id, project.owner)))

    updateTimeSortingOption match {
      case Some(updateTimeSorting) =>
        if(updateTimeSorting) projectsWithTasks.sortBy(findLastUpdate)
        else projectsWithTasks.sortBy(findLastUpdate).reverse

      case None => projectsWithTasks
    }
  }

}
