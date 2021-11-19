package utils

import akka.actor.ActorSystem.Version
import akka.util.Timeout
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig
import utils.database.DatabaseConnector

import java.net.InetAddress.getLocalHost

object InMemoryPostgresStorage {
  def jdbcUrl: String = "jdbc:postgresql://localhost/postgres"
  def username: String = "postgres"
  def password: String = "admin"
  def host: String = "0.0.0.0"
  def port: Int = 9000

  val databaseConnector = new DatabaseConnector(
    InMemoryPostgresStorage.jdbcUrl,
    InMemoryPostgresStorage.username,
    InMemoryPostgresStorage.password
  )
}
