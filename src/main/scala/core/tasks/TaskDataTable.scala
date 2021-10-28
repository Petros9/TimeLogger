package core.tasks

import core.Task
import utils.database.DatabaseConnector

private[tasks] trait TaskDataTable {
  protected val databaseConnector: DatabaseConnector
  import databaseConnector.profile.api._

  class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
    def id            = column[String]("id", O.PrimaryKey)
    def projectId     = column[String]("project_id")
    def startPointer  = column[Long]("start_pointer")
    def volume        = column[Int]("volume")
    def workingTime   = column[Long]("working_time")
    def desc          = column[String]("desc")
    def endPointer    = column[Long]("end_pointer")
    def * = (id, projectId, startPointer, volume, workingTime, desc, endPointer) <> ((Task.apply _).tupled, Task.unapply)
  }

  protected val tasks = TableQuery[Tasks]

}