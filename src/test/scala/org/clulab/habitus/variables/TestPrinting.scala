package org.clulab.habitus.variables

import org.clulab.habitus.utils.Test
import org.clulab.utils.FileUtils
import org.json4s.{DefaultFormats, JArray, JObject}
import org.json4s.jackson.JsonMethods

import java.io.File

class TestPrinting extends Test {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  val inputDir = "./src/test/resources"
  val outputDir = "."
  val threads = 2
  val tsvOutputFile = "./mentions.tsv"
  val jsonOutputFile = "./mentions.json"
  val jsonlOutputFile = "./mentions.jsonl"
  val masterResource = "./src/main/resources/variables/master.yml"

  new File(tsvOutputFile).delete()
  new File(jsonOutputFile).delete()
  new File(jsonlOutputFile).delete()
  VariableReader.run(VariableProcessor(), inputDir, outputDir, threads)

  behavior of "TsvPrinter"

  it should "print something valid and non-empty" in {
    val tsv = FileUtils.getTextFromFile(tsvOutputFile)
    val hasTabs = tsv.contains("\t")

    tsv should not be empty
    hasTabs should be (true)
  }

  behavior of "JsonPrinter"

  ignore should "print something valid and non-empty" in {
    val json = FileUtils.getTextFromFile(jsonOutputFile)
    val jValue = JsonMethods.parse(json)
    val jValues = jValue.extract[JArray].arr

    jValues should not be empty
  }

  behavior of "JsonlPrinter"

  it should "print something valid and non-empty" in {
    val jsonls = FileUtils.getTextFromFile(jsonlOutputFile).split('\n')

    jsonls.foreach { json =>
      val jValue = JsonMethods.parse(json)
      val jObject = jValue.extract[JObject].obj

      jObject should not be empty
    }
  }
}
