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

  case class NameOccupiedResponse(message: String = "ERROR, This name is already in use")
  case class NoResourceResponse(message: String = "ERROR, No resource found")
  case class NotAuthorisedResponse(message: String = "ERROR, Unauthorised")
  case class TimeConflictResponse(message: String = "ERROR, Task time conflict")
}
