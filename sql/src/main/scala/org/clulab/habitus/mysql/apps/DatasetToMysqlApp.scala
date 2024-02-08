package org.clulab.habitus.mysql.apps

import org.clulab.habitus.mysql.apps.utils.Credentials

import java.sql.{Connection, DriverManager}
import scala.util.Using

object DatasetToMysqlApp extends App {
  val credentialsFilename = "../credentials/mysql-credentials.properties"
  val indexName = "habitus"
  val url = s"jdbc:mysql://localhost:3306/$indexName?serverTimezone=UTC"
  val credentials = new Credentials(credentialsFilename)

  def runQuery(connection: Connection): Unit = {
    val statement = "SELECT * FROM region"
    val preparedStatement = connection.prepareStatement(statement)
    val resultSet = preparedStatement.executeQuery()

    while (resultSet.next) {
      val name = resultSet.getString("name")

      println(name)
    }
  }

  def run(connection: Connection): Unit = {
    runQuery(connection)
  }

  try {
    val connection = DriverManager.getConnection(url, credentials.username, credentials.password)

    Using.resource(connection) { connection =>
      run(connection)
    }
    println("Goodbye")
  }
  catch {
    case throwable: Throwable =>
      println(throwable)
  }
}
