package org.clulab.habitus.apps.tpi

import ai.lum.common.FileUtils._
import com.typesafe.config.ConfigValueFactory
import org.clulab.utils.ThreadUtils
import org.clulab.wm.eidos.EidosSystem
import org.clulab.wm.eidos.serialization.jsonld.JLDCorpus
import org.clulab.wm.eidoscommon.utils.{FileEditor, FileUtils}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import scala.util.Using

object Step1OutputEidos extends App {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  val baseDirectoryName = args.lift(0).getOrElse("../corpora/uganda/karamoja")
  val files = new File(baseDirectoryName).listFilesByWildcard("*.json", recursive = true).toList
  val config =  EidosSystem.defaultConfig
      .withValue("ontologies.useGrounding", ConfigValueFactory.fromAnyRef(false))
  val eidosSystem = new EidosSystem(config)
  val count = new AtomicInteger()
  val parFiles = ThreadUtils.parallelize(files, 8)

  parFiles.foreach { file =>
    try {
      val path = FileEditor(file.getCanonicalFile).setExt("jsonld").get
      println(s"${count.getAndIncrement()} ${file.getCanonicalPath} -> ${path.getCanonicalPath}")

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
