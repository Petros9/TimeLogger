package utils.database

class Config {
  def jdbcUrl: String = "jdbc:postgresql://localhost/postgres"
  def username: String = "postgres"
  def password: String = "admin"
  def host: String = "0.0.0.0"
  def port: Int = 9000
  def secretKey:String = "secret"
}