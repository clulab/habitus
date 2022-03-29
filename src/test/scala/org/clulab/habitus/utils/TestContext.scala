package org.clulab.habitus.utils

import org.clulab.serialization.json.stringify
import org.clulab.utils.Closer.AutoCloser

import java.io.File

class TestContext extends Test {

  case class TestContext(string: String, floatOpt: Option[Float], stringOpt: Option[String]) extends Context

  behavior of "Context"

  it should "print Some for json" in {
    val context = TestContext("string", Some(.5f), Some("some string"))
    val argValuePairs = context.getArgValuePairs

    argValuePairs(0) should be ("string" -> "string")
    argValuePairs(1) should be ("floatOpt" -> Some(.5f))
    argValuePairs(2) should be ("stringOpt" -> Some("some string"))

    val tmpFile = File.createTempFile(getClass.getSimpleName, null)
    new JsonPrinter(tmpFile.toString).autoClose { printer =>
      val jObject = printer.toJObject(argValuePairs)
      val json = stringify(jObject, pretty = false)

      json should be ("""{"string":"string","floatOpt":0.5,"stringOpt":"some string"}""")
    }
    tmpFile.delete()
  }

  it should "print Some for jsonl" in {
    val context = TestContext("string", Some(.5f), Some("some string"))
    val argValuePairs = context.getArgValuePairs

    argValuePairs(0) should be ("string" -> "string")
    argValuePairs(1) should be ("floatOpt" -> Some(.5f))
    argValuePairs(2) should be ("stringOpt" -> Some("some string"))

    val tmpFile = File.createTempFile(getClass.getSimpleName, null)
    new JsonlPrinter(tmpFile.toString).autoClose { printer =>
      val jObject = printer.toJObject(argValuePairs)
      val json = stringify(jObject, pretty = false)

      json should be ("""{"string":"string","floatOpt":0.5,"stringOpt":"some string"}""")
    }
    tmpFile.delete()
  }

  it should "print Some for tsv" in {
    val context = TestContext("string", Some(0.5f), Some("some string"))
    val header = context.getTSVContextHeader()
    val string = context.getTSVContextString()

    header should be ("string\tfloatOpt\tstringOpt")
    string should be ("string\t0.5\tsome string")
  }

  it should "not print None for json" in {
    val context = TestContext("string", None, None)
    val argValuePairs = context.getArgValuePairs

    argValuePairs(0) should be ("string" -> "string")
    argValuePairs(1) should be ("floatOpt" -> None)
    argValuePairs(2) should be ("stringOpt" -> None)

    val tmpFile = File.createTempFile(getClass.getSimpleName, null)
    new JsonPrinter(tmpFile.toString).autoClose { printer =>
      val jObject = printer.toJObject(argValuePairs)
      val json = stringify(jObject, pretty = false)

      json should be ("""{"string":"string"}""")
    }
    tmpFile.delete()
  }

  it should "not print None for jsonl" in {
    val context = TestContext("string", None, None)
    val argValuePairs = context.getArgValuePairs

    argValuePairs(0) should be ("string" -> "string")
    argValuePairs(1) should be ("floatOpt" -> None)
    argValuePairs(2) should be ("stringOpt" -> None)

    val tmpFile = File.createTempFile(getClass.getSimpleName, null)
    new JsonlPrinter(tmpFile.toString).autoClose { printer =>
      val jObject = printer.toJObject(argValuePairs)
      val json = stringify(jObject, pretty = false)

      json should be ("""{"string":"string"}""")
    }
    tmpFile.delete()
  }

  it should "not print None for tsv" in {
    val context = TestContext("string", None, None)
    val header = context.getTSVContextHeader()
    val string = context.getTSVContextString()

    header should be ("string\tfloatOpt\tstringOpt")
    string should be ("string\t\t")
  }
}
