package core.projects

import core.Project
import utils.database.DatabaseConnector

import scala.concurrent.{ExecutionContext, Future}

sealed trait ProjectDataStorage {

  def getProjects(): Future[Seq[Project]]

  def getProject(id: String): Future[Option[Project]]

  def saveProject(profile: Project): Future[Project]

}

class JdbcProjectDataStorage(val databaseConnector: DatabaseConnector)(implicit executionContext: ExecutionContext)
  extends ProjectDataTable
    with ProjectDataStorage {

  import databaseConnector._
  import databaseConnector.profile.api._

  def getProjects(): Future[Seq[Project]] = db.run(projects.result)

  def getProject(id: String): Future[Option[Project]] = db.run(projects.filter(_.id === id).result.headOption)

  def saveProject(project: Project): Future[Project] =
    db.run(projects.insertOrUpdate(project)).map(_ => project)
}

class InMemoryUserProfileStorage extends ProjectDataStorage {

  private var state: Seq[Project] = Nil

  override def getProjects(): Future[Seq[Project]] =
    Future.successful(state)

  override def getProject(id: String): Future[Option[Project]] =
    Future.successful(state.find(_.id == id))

  override def saveProject(project: Project): Future[Project] =
    Future.successful {
      state = state.filterNot(_.id == project.id)
      state = state :+ project
      project
    }

}