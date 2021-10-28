package core.projects

import core.{Project, ProjectId, UserId}
import utils.database.DatabaseConnector

import scala.concurrent.{ExecutionContext, Future}

sealed trait ProjectDataStorage {

  def getProjects(token: UserId): Future[Seq[Project]]

  def getProject(id: ProjectId): Future[Option[Project]]

  def saveProject(project: Project): Future[Project]

  def nameIsFreeToBeUsed(newProjectName: String): Future[Option[Project]]

}

class JdbcProjectDataStorage(val databaseConnector: DatabaseConnector)(implicit executionContext: ExecutionContext)
  extends ProjectDataTable
    with ProjectDataStorage {

  import databaseConnector._
  import databaseConnector.profile.api._

  def getProjects(token: UserId): Future[Seq[Project]] = db.run(projects.filter(_.owner === token).result)

  def getProject(id: ProjectId): Future[Option[Project]] = db.run(projects.filter(_.id === id).result.headOption)

  def saveProject(project: Project): Future[Project] =
    db.run(projects.insertOrUpdate(project)).map(_ => project)




  // VALIDATE FUNCTIONS
  def nameIsFreeToBeUsed(newProjectName: String): Future[Option[Project]] = db.run(projects.filter(_.projectName === newProjectName).result.headOption)

}

class InMemoryUserProjectStorage extends ProjectDataStorage {

  private var state: Seq[Project] = Nil

  override def getProjects(token: UserId): Future[Seq[Project]] =
    Future.successful(state.filter(_.owner == token))

  override def getProject(id: ProjectId): Future[Option[Project]] =
    Future.successful(state.find(_.id == id))

  override def saveProject(project: Project): Future[Project] =
    Future.successful {
      state = state.filterNot(_.id == project.id)
      state = state :+ project
      project
    }

  def nameIsFreeToBeUsed(newProjectName: String): Future[Option[Project]] =
    Future.successful(state.find(_.projectName == newProjectName))

}
