package org.clulab.habitus.utils

import java.io.{PrintWriter, StringWriter}
import scala.util.Using

class XsvTest extends Test {

  behavior of "TsvReader and TsvWriter"

  it should "escape correctly" in {
    val stringWriter = new StringWriter()
    val expectedString = "testing\\n1, 2, 3"

    val intermediateString = Using.resource(new PrintWriter(stringWriter)) { printWriter =>
      val tsvWriter = new TsvWriter(printWriter)

      tsvWriter.escape(expectedString)
    }
    val actualString = {
      val tsvReader = new TsvReader()

      tsvReader.unescape(intermediateString)
    }

    actualString should be (expectedString)
  }
}
