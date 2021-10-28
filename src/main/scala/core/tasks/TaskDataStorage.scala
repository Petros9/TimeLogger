package core.tasks

import core.Task
import utils.database.DatabaseConnector

import scala.concurrent.{ExecutionContext, Future}

sealed trait TaskDataStorage {

  def getTasks: Future[Seq[Task]]

  def getTask(id: String): Future[Option[Task]]

  def saveTask(task: Task): Future[Task]

}

class JdbcTaskDataStorage(val databaseConnector: DatabaseConnector)(implicit executionContext: ExecutionContext)
  extends TaskDataTable
    with TaskDataStorage {

  import databaseConnector._
  import databaseConnector.profile.api._

  def getTasks(): Future[Seq[Task]] = db.run(tasks.result)

  def getTask(id: String): Future[Option[Task]] = db.run(tasks.filter(_.id === id).result.headOption)

  def saveTask(task: Task): Future[Task] =
    db.run(tasks.insertOrUpdate(task)).map(_ => task)
}

class InMemoryUserTaskStorage extends TaskDataStorage {

  private var state: Seq[Task] = Nil

  override def getTasks(): Future[Seq[Task]] =
    Future.successful(state)

  override def getTask(id: String): Future[Option[Task]] =
    Future.successful(state.find(_.id == id))

  override def saveTask(task: Task): Future[Task] =
    Future.successful {
      state = state.filterNot(_.id == task.id)
      state = state :+ task
      task
    }

}
