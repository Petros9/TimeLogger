package core.projects

import akka.http.scaladsl.server.Directives.complete
import core.{ProjectId, UserId}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ProjectValidator (projectDataStorage: ProjectDataStorage){
  def isOwner(projectId: ProjectId, token: UserId): Boolean = {
    println(projectId)
    val projectOption = Await.result(projectDataStorage.getProject(projectId), Duration.Inf)
    projectOption match {
      case Some(project) =>
        println(project.owner + "####")
        project.owner.equals(token)
      case None =>
        println("!!!!!")
        false
    }
  }

  def nameIsFreeToBeUsed(newProjectNameOption: Option[String]): Boolean = {
    newProjectNameOption match {
      case Some(newProjectName) =>
        val projectOption = Await.result(projectDataStorage.nameIsFreeToBeUsed(newProjectName), Duration.Inf)

        projectOption match {
          case Some(_) => false
          case None => true
        }

      case None => true
    }

  }

  def isNotDeleted(projectId: ProjectId): Boolean = {
    val projectOption = Await.result(projectDataStorage.getProject(projectId), Duration.Inf)

    projectOption match {
      case Some(project) => project.endPointer.equals(0L)
      case None => false
    }
  }

}
