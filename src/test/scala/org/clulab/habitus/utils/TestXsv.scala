package org.clulab.habitus.utils

import java.io.{PrintWriter, StringWriter}
import scala.util.Using

class TestXsv extends Test {

  behavior of "TsvReader and TsvWriter"

  it should "escape \\n correctly" in {
    val stringWriter = new StringWriter()
    val expectedString = "between 30 a\\nd 40 tonnes daily"

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

  it should "escape \\t correctly" in {
    val stringWriter = new StringWriter()
    val expectedString = "the Civil service\\the Audit Service"

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

  it should "catch invalid escapes inside a string" in {
    assertThrows[RuntimeException] {
      val tsvReader = new TsvReader()

      // Single escapement characters are not allowed.
      // This will not unescape the following space.
      // It will be left over and cause complaints.
      tsvReader.unescape("some \\\\\\ thing")
    }
  }

  it should "catch invalid escapes at the end of a string" in {
    assertThrows[RuntimeException] {
      val tsvReader = new TsvReader()

      // Single escapement characters are not allowed.
      // This will not unescape the following space.
      // It will be left over and cause complaints.
      tsvReader.unescape("some thing \\")
    }
  }
}
