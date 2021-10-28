package utils

package object exceptions {
  class NameOccupiedException extends Exception

  class NoResourceException extends Exception

  class NotAuthorisedException extends Exception

  class TimeConflictException extends Exception


  case class NameOccupiedResponse(code: Int = 403, message: String = "ERROR, This name is already in use")
  case class NoResourceResponse(code: Int = 404, message: String = "ERROR, No resource found")
  case class NotAuthorisedResponse(code: Int = 401, message: String = "ERROR, Unauthorised")
  case class TimeConflictResponse(code: Int = 403, message: String = "ERROR, Task time conflict")
}
