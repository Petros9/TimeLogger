package core.tasks

import core.{ProjectId, Task}
import utils.database.DatabaseConnector

import scala.concurrent.{ExecutionContext, Future}

sealed trait TaskDataStorage {

  def getTask(id: String): Future[Option[Task]]

  def saveTask(task: Task): Future[Task]

  def getProjectTasks(projectId: ProjectId): Future[Seq[Task]]

}

class JdbcTaskDataStorage(val databaseConnector: DatabaseConnector)(implicit executionContext: ExecutionContext)
  extends TaskDataTable
    with TaskDataStorage {

  import databaseConnector._
  import databaseConnector.profile.api._

  def getProjectTasks(projectId: ProjectId): Future[Seq[Task]] = db.run(tasks.filter(_.projectId === projectId).result)

  def getTask(id: String): Future[Option[Task]] = db.run(tasks.filter(_.id === id).result.headOption)

  def saveTask(task: Task): Future[Task] =
    db.run(tasks.insertOrUpdate(task)).map(_ => task)
}

class InMemoryTaskStorage extends TaskDataStorage {

  private var state: Seq[Task] = Nil

  override def getProjectTasks(projectId: ProjectId): Future[Seq[Task]] =
    Future.successful(state.filter(_.projectId == projectId))

  override def getTask(id: String): Future[Option[Task]] =
    Future.successful(state.find(_.id == id))


  override def saveTask(task: Task): Future[Task] =
    Future.successful {
      state = state.filterNot(_.id == task.id)
      state = state :+ task
      task
    }

}
