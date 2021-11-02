package org.clulab.habitus.variables

import org.clulab.habitus.utils.Test
import org.clulab.utils.FileUtils
import org.json4s.{DefaultFormats, JArray}
import org.json4s.jackson.JsonMethods

class TestPrinting extends Test {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  behavior of "JsonPrinter"

  it should "print something valid and non-empty" in {
    val inputDir = "./src/test/resources"
    val outputDir = "."
    val threads = 2

    VariableReader.run(inputDir, outputDir, threads)

    val json = FileUtils.getTextFromFile("./mentions.json")
    val jValue = JsonMethods.parse(json)
    val jValues = jValue.extract[JArray].arr

    jValues should not be empty
  }
}
