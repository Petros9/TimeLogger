package object core {

  type UserId    = String
  type AuthToken = String
  type ProjectId = String
  type TaskId = String

  final case class AuthTokenContent(userId: UserId)

  final case class AuthData(id: UserId, username: String) {
    require(id.nonEmpty, "id.empty")
    require(username.nonEmpty, "username.empty")
  }

  final case class Project(id: ProjectId, projectName: String, startPointer: Long, endPointer: Long = 0, owner: UserId) {
    require(id.nonEmpty, "id.empty")
    require(projectName.nonEmpty, "name.empty")
    require(!startPointer.isNaN, "start.pointer.empty")
    require(owner.nonEmpty, "owner.empty")
  }

  final case class ProjectDataUpdate(projectName: Option[String] = None, endPointer: Option[Long] = None) {
    def merge(project: Project): Project =
      Project(project.id, projectName.getOrElse(project.projectName), project.startPointer, endPointer.getOrElse(project.endPointer), project.owner)
  }

  final case class Task(id: TaskId, projectId: ProjectId, startPointer: Long, volume: Int, workingTime: Long,  desc: String, endPointer: Long = 0) {
    require(id.nonEmpty, "id.empty")
    require(projectId.nonEmpty, "project.empty")
    require(!startPointer.isNaN, "start.pointer.empty")
    require(!workingTime.isNaN, "working.time.empty")
  }

  final case class TaskDataUpdate(startPointer: Option[Long] = None, volume: Option[Int] = None, workingTime: Option[Long],  desc: Option[String] = None, endPointer: Option[Long] = None) {
    def merge(task: Task): Task =
      Task(task.id, task.projectId, startPointer.getOrElse(task.startPointer), volume.getOrElse(task.volume), workingTime.getOrElse(task.workingTime), desc.getOrElse(task.desc), endPointer.getOrElse(task.endPointer))
  }

  case class ProjectsFilters(idList: Option[Seq[ProjectId]], startTime: Option[Long], endTime: Option[Long], deleted: Option[Boolean], creationTimeIncSorting: Option[Boolean], updateTimeIncSorting: Option[Boolean])
}
