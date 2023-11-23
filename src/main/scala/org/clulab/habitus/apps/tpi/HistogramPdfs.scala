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

object HistogramPdfs extends App {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  val baseDirectoryName = args.lift(0).getOrElse("../corpora/uganda")
  val inFiles = new File(baseDirectoryName)
    .listFilesByWildcard("*.json", recursive = true)
    .filter { jsonFile =>
      val pdfFile = FileEditor(jsonFile.getCanonicalFile).setExt("pdf").get

      pdfFile.exists
    }
    .toVector
    .sortBy { inFile => inFile.getName }
  val count = new AtomicInteger()
  val parFiles = ThreadUtils.parallelize(inFiles, 5)
  val uganda = "uganda"

  parFiles.foreach { inFile =>
    try {
      val json = FileUtils.getTextFromFile(inFile)
      val jValue = JsonMethods.parse(json)
      val text = (jValue \ "text").extract[String]
      val ugandaCount = text.toLowerCase.sliding(uganda.length).count(_ == uganda)

      println(s"${count.getAndIncrement()}/${inFiles.length}\t${inFile.getCanonicalPath}\t$ugandaCount")
    }
    catch {
      case throwable: Throwable =>
        println(s"Processing of ${inFile.getCanonicalPath} failed: ${throwable.getMessage}")
    }
  }
}
