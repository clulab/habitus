package org.clulab.habitus.apps.tpi

import ai.lum.common.FileUtils._
import com.typesafe.config.ConfigValueFactory
import org.clulab.wm.eidos.EidosSystem
import org.clulab.wm.eidos.serialization.jsonld.JLDCorpus
import org.clulab.wm.eidoscommon.utils.{FileEditor, FileUtils}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.io.{File, PrintWriter, StringWriter}
import java.util.concurrent.atomic.AtomicInteger
import scala.util.Using

object Step1OutputEidosWithHolidays extends App {
  val filter = false
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  val badHolidays = Seq(
    ("Thanksgiving", "Thank_giving"),
    ("Easter", "East_r"),
    ("Labour", "Labo_r"),
    ("LABOUR", "LABO_R")
  )

  def withoutBadHolidays(text: String): String = {
    if (!filter) text
    else {
      val withoutBad = badHolidays.foldLeft(text) { case (text, (oldSpelling, newSpelling)) =>
        text.replace(oldSpelling, newSpelling)
      }

      withoutBad
    }
  }

  def withBadHolidays(text: String): String = {
    if (!filter) text
    else {
      val withBad = badHolidays.foldLeft(text) { case (text, (oldSpelling, newSpelling)) =>
        text.replace(newSpelling, oldSpelling)
      }

      withBad
    }
  }

  val baseDirectoryName = args.lift(0).getOrElse("../corpora/uganda")
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
//  val parFiles = ThreadUtils.parallelize(inAndOutFiles, 1)

  inAndOutFiles.foreach { case (inFile, outFile) =>
    try {
      println(s"${count.getAndIncrement()}/${inAndOutFiles.length} (${inFile.length}) ${inFile.getCanonicalPath} -> ${outFile.getCanonicalPath}")

      val json = FileUtils.getTextFromFile(inFile)
      val jValue = JsonMethods.parse(json)
      val text = withoutBadHolidays((jValue \ "text").extract[String])
      val annotatedDocument = eidosSystem.extractFromText(text)

      Using.resource(FileUtils.printWriterFromFile(outFile)) { printWriter =>
        val stringWriter = new StringWriter()
        val stringPrintWriter = new PrintWriter(stringWriter)
        new JLDCorpus(annotatedDocument).serialize(stringPrintWriter, regrounding = false)
        val json = withBadHolidays(stringWriter.toString)

        printWriter.println(json)
      }
    }
    catch {
      case throwable: Throwable =>
        println(s"Processing of ${inFile.getCanonicalPath} failed: ${throwable.getMessage}")
    }
  }
}
