package org.clulab.habitus.elasticsearch.utils

import java.io.FileInputStream
import java.util.Properties
import scala.util.Using

class Credentials(filename: String) {
  val (username, password) = Using.resource(new FileInputStream(filename)) { inputStream =>
    val properties = {
      val properties = new Properties()

      properties.load(inputStream)
      properties
    }
    val username = properties.getProperty("username")
    val password = properties.getProperty("password")

    (username, password)
  }
}
