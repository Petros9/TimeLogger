package core.tasks

import core.projects.ProjectValidator
import core.{Task, TaskDataUpdate}
import utils.MonadTransformers.FutureOptionMonadTransformer

import scala.concurrent.{ExecutionContext, Future}

class TaskService(taskDataStorage: TaskDataStorage, taskValidator: TaskValidator, projectValidator: ProjectValidator)
                    (implicit executionContext: ExecutionContext) {

  def deleteTask(id: String, token: String): Future[Option[Task]] = {
    updateTask(id, TaskDataUpdate(None, None, None, None, Option(System.currentTimeMillis())), token)
  }
  def getTasks: Future[Seq[Task]] =
    taskDataStorage.getTasks

  def getTask(id: String): Future[Option[Task]] =
    taskDataStorage.getTask(id)

  def createTask(task: Task): Future[Task] = {
    if(projectValidator.isNotDeleted(task.projectId) && taskValidator.timeDoesNotOverlap(Option(task.startPointer), Option(task.workingTime), Option(task.projectId))){
      taskDataStorage.saveTask(task)
    }
  }

  def updateTask(id: String, taskUpdate: TaskDataUpdate, token: String): Future[Option[Task]] = {
    val optionTaskToBeUpdated = taskValidator.getTaskToBeUpdated(id)
    optionTaskToBeUpdated match {
      case Some(task) =>
        if(taskValidator.isOwner(id, token) && taskValidator.isNotDeleted(id) && taskValidator.timeDoesNotOverlap(taskUpdate.startPointer, taskUpdate.workingTime, task.projectId)) {
          taskDataStorage
            .getTask(id)
            .mapT(taskUpdate.merge)
            .flatMapTOuter(taskDataStorage.saveTask)
        }
    }

  }

}
