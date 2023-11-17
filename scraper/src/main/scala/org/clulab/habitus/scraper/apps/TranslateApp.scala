package org.clulab.habitus.scraper.apps

import org.clulab.pdf2txt.common.utils.FileEditor
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.FileUtils
import org.json4s.JObject
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods

object TranslateApp extends App {
  val dir = args.lift(0).getOrElse("../corpora/uganda/uganda china/articles/nbs_ug/Lugandan")
  val jsonFiles = FileUtils.findFiles(dir, "json")

  def canGetTwoLines(lines: Iterator[String]): Boolean = {
    val text = lines.next()

    if (text.nonEmpty) {
      lines.next()
      true
    }
    else
      false
  }

  jsonFiles.foreach { jsonFile =>
    val json = FileUtils.getTextFromFile(jsonFile)
    val txtFile = FileEditor(jsonFile).setExt("txt").get
    val text = FileUtils.getTextFromFile(txtFile)
    val lines = text.split('\n').iterator
    val english = {
      while (canGetTwoLines(lines)) { }
      val english = lines.mkString("\n")

      println(english)
      english
    }
    val jObject = JsonMethods.parse(json).asInstanceOf[JObject]
    val newJObject = jObject.mapField { case (name, value) =>
      if (name == "text") "lugandan" -> value
      else name -> value
    }.asInstanceOf[JObject] ~ ("text" -> english)
    val newJson = JsonMethods.pretty(newJObject)
    val newJsonFile = FileEditor(jsonFile).setExt("json2").get

    FileUtils.printWriterFromFile(newJsonFile).autoClose { printWriter =>
      printWriter.println(newJson)
    }
  }
}
