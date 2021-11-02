package utils

import core.{Project, Task}

package object responses {
  class NameOccupiedException extends Exception

  class NoResourceException extends Exception

  class NotAuthorisedException extends Exception

  class TimeConflictException extends Exception

  case class ProjectInfoResponse(project: Project, tasks: Seq[Task], sumProjectTime: Long)

  case class ProjectInfoResponseWithoutTime(project: Project, tasks: Seq[Task]) {
    def findLastUpdate: Long = {
      if(tasks.isEmpty) project.startPointer
      else tasks.maxBy(_.startPointer).startPointer
    }
  }

  case class NameOccupiedResponse(code: Int = 403, message: String = "ERROR, This name is already in use")
  case class NoResourceResponse(code: Int = 404, message: String = "ERROR, No resource found")
  case class NotAuthorisedResponse(code: Int = 401, message: String = "ERROR, Unauthorised")
  case class TimeConflictResponse(code: Int = 403, message: String = "ERROR, Task time conflict")
}
