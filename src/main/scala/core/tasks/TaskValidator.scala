package core.tasks

import core.projects.ProjectValidator
import core.{ProjectId, Task, TaskId, UserId}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TaskValidator(taskDataStorage: TaskDataStorage, projectValidator: ProjectValidator) {

  def isOwner(taskId: TaskId, token: UserId): Boolean = {
    val taskOption = Await.result(taskDataStorage.getTask(taskId), Duration.Inf)

    taskOption match {
      case Some(task) => projectValidator.isOwner(task.projectId, token)
      case None => false
    }
  }

  def timeDoesNotOverlap(startPointer: Option[Long], workingTime: Option[Long], projectId: ProjectId): Boolean = {
    ???
  }

  def getTaskToBeUpdated(taskId: TaskId): Option[Task] = {
    Await.result(taskDataStorage.getTask(taskId), Duration.Inf)
  }

  def isNotDeleted(taskId: TaskId): Boolean = {
    val taskOption = Await.result(taskDataStorage.getTask(taskId), Duration.Inf)

    taskOption match {
      case Some(task) => task.endPointer.equals(0L) && projectValidator.isNotDeleted(task.projectId)
      case None => false
    }
  }

}
