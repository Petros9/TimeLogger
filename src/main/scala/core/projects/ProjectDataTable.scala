package core.projects

import core.Project
import utils.database.DatabaseConnector


private[projects] trait ProjectDataTable {

  protected val databaseConnector: DatabaseConnector
  import databaseConnector.profile.api._

  class Projects(tag: Tag) extends Table[Project](tag, "projects") {
    def id        = column[String]("id", O.PrimaryKey)
    def projectName = column[String]("project_name")
    def startPointer  = column[Long]("start_pointer")
    def endPointer  = column[Long]("end_pointer")

    def * = (id, projectName, startPointer, endPointer) <> ((Project.apply _).tupled, Project.unapply)
  }

  protected val projects = TableQuery[Projects]

}
