package org.clulab.sbt

object BuildUtils {
  val balaur = false

  val (procVer, eidosVer, pdf2txtVer) =
      if (balaur) {
        val procVer = "9.0.0-RC4"
        val eidosVer = "1.8.0-RC4"
        val pdf2txtVer = "1.2.0-RC4"

        (procVer, eidosVer, pdf2txtVer)
      }
      else {
        val procVer = "8.5.4"
        val eidosVer = "1.7.0"
        val pdf2txtVer = "1.1.6"

        (procVer, eidosVer, pdf2txtVer)
      }
}