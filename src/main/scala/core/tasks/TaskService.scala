package core.tasks

import core.projects.ProjectValidator
import core.{ProjectId, Task, TaskDataUpdate}
import utils.MonadTransformers.FutureOptionMonadTransformer
import utils.responses.{NoResourceException, NotAuthorisedException, TimeConflictException}

import scala.concurrent.{ExecutionContext, Future}

class TaskService(taskDataStorage: TaskDataStorage, taskValidator: TaskValidator, projectValidator: ProjectValidator)
                    (implicit executionContext: ExecutionContext) {

  def deleteTask(id: String, token: String): Future[Option[Task]] = {
    updateTask(id, TaskDataUpdate(None, None, None, None, Option(System.currentTimeMillis())), token)
  }


  @throws(classOf[NotAuthorisedException])
  def getProjectTasks(projectId: ProjectId, token: String): Future[Seq[Task]] = {
    if(projectValidator.isOwner(projectId, token)){
      taskDataStorage.getProjectTasks(projectId)
    } else {
      throw new NotAuthorisedException
    }
  }

  def getTask(id: String): Future[Option[Task]] =
    taskDataStorage.getTask(id)

  @throws(classOf[NoResourceException])
  @throws(classOf[TimeConflictException])
  def createTask(task: Task): Future[Task] = {
    if(!projectValidator.isNotDeleted(task.projectId)){
      throw new NoResourceException
    } else if(!taskValidator.timeDoesNotOverlap(task.startPointer, task.workingTime, task.projectId, task.id)){
      throw new TimeConflictException
    } else {
      taskDataStorage.saveTask(task)
    }
  }


  @throws(classOf[NotAuthorisedException])
  @throws(classOf[NoResourceException])
  @throws(classOf[TimeConflictException])
  def updateTask(id: String, taskUpdate: TaskDataUpdate, token: String): Future[Option[Task]] = {
    val optionTaskToBeUpdated = taskValidator.getTaskToBeUpdated(id)
    optionTaskToBeUpdated match {
      case Some(task) =>
        if(!taskValidator.isOwner(id, token)){
          throw new NotAuthorisedException
        } else if(!taskValidator.isNotDeleted(id)){
          throw new NoResourceException
        } else if(!taskValidator.timeDoesNotOverlap(taskUpdate.startPointer.getOrElse(taskValidator.getTaskToBeUpdated(id).get.startPointer),
                                                    taskUpdate.workingTime.getOrElse(taskValidator.getTaskToBeUpdated(id).get.workingTime),
                                                    task.projectId, task.id)) {
          throw new TimeConflictException
        } else {
          taskDataStorage
            .getTask(id)
            .mapT(taskUpdate.merge)
            .flatMapTOuter(taskDataStorage.saveTask)
        }

      case None => throw new NoResourceException
    }

  }

}
