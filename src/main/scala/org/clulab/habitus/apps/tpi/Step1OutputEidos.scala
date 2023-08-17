package org.clulab.habitus.apps.tpi

import ai.lum.common.FileUtils._
import org.clulab.wm.eidos.EidosSystem
import org.clulab.wm.eidos.serialization.jsonld.JLDCorpus
import org.clulab.wm.eidoscommon.utils.{FileEditor, FileUtils}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.io.File
import scala.util.Using

object Step1OutputEidos extends App {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  val baseDirectoryName = "../corpora/multi"
  val files = new File(baseDirectoryName).listFilesByWildcard("*.json", recursive = true)
  val eidosSystem = new EidosSystem()

  files.zipWithIndex.par.foreach { case (file, index) =>
    try {
      val path = FileEditor(file.getCanonicalFile).setExt("jsonld").get
      println(s"$index ${file.getCanonicalPath} -> ${path.getCanonicalPath}")

      val json = FileUtils.getTextFromFile(file)
      val jValue = JsonMethods.parse(json)
      val text = (jValue \ "text").extract[String]
      val annotatedDocument = eidosSystem.extractFromText(text)

      Using.resource(FileUtils.printWriterFromFile(path)) { printWriter =>
        new JLDCorpus(annotatedDocument).serialize(printWriter, regrounding = false)
      }
    }
    catch {
      case throwable: Throwable =>
        println(s"Processing of ${file.getCanonicalPath} failed: ${throwable.getMessage}")
    }
  }
}
