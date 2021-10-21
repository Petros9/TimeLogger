package core.auth

import core.AuthData
import slick.lifted.Tag
import utils.database.DatabaseConnector

private[auth] trait AuthDataTable {

  protected val databaseConnector: DatabaseConnector
  import databaseConnector.profile.api._

  class AuthDataSchema(tag: Tag) extends Table[AuthData](tag, "auth") {
    def id       = column[String]("id", O.PrimaryKey)
    def username = column[String]("username")

    def * = (id, username) <> ((AuthData.apply _).tupled, AuthData.unapply)
  }

  protected val auth = TableQuery[AuthDataSchema]

}
