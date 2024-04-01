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

  val baseDirectoryName = args.lift(0).getOrElse("../corpora/uganda/interview/articles")
  val inAndOutFiles = new File(baseDirectoryName)
      .listFilesByWildcard("*.json", recursive = true)
      .map { inFile =>
        val outFile = FileEditor(inFile.getCanonicalFile).setExt("jsonld").get

        (inFile, outFile)
      }
      .filterNot { case (_, outFile) => outFile.exists }
      .toVector
      .sortBy { case (inFile, _) => inFile.length }
  val config =  EidosSystem.defaultConfig
      .withValue("ontologies.useGrounding", ConfigValueFactory.fromAnyRef(false))
  val eidosSystem = new EidosSystem(config)
  val count = new AtomicInteger()
  val parFiles = inAndOutFiles // ThreadUtils.parallelize(inAndOutFiles, 2)

  parFiles.foreach { case (inFile, outFile) =>
    try {
      println(s"${count.getAndIncrement()}/${inAndOutFiles.length} ${inFile.getCanonicalPath} -> ${outFile.getCanonicalPath}")

      val json = FileUtils.getTextFromFile(inFile)
      val jValue = JsonMethods.parse(json)
      val text = (jValue \ "text").extract[String]
      val annotatedDocument = eidosSystem.extractFromText(text)

      Using.resource(FileUtils.printWriterFromFile(outFile)) { printWriter =>
        new JLDCorpus(annotatedDocument).serialize(printWriter, regrounding = false)
      }
    }
    catch {
      case throwable: Throwable =>
        println(s"Processing of ${inFile.getCanonicalPath} failed: ${throwable.getMessage}")
    }
  }
}
