package org.clulab.habitus.apps.elasticsearch

import ai.lum.common.FileUtils._
import org.clulab.habitus.apps.utils.JsonRecord
import org.clulab.utils.Sourcer
import org.clulab.wm.eidoscommon.utils.{FileEditor, FileUtils, TsvReader}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.io.File
import scala.util.Using

object VerifyGhanaApp extends App {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  val datasetFilename = args.lift(0).getOrElse("../corpora/ghana-multimix/dataset55k.tsv")
  val jsonDirname = args.lift(1).getOrElse("/home/kwa/data/Corpora/habitus-project/corpora/multimix")

  def jsonFileToJsonld(jsonFile: File): File =
      FileEditor(jsonFile).setExt("jsonld").get

  def jsonFileToRecord(jsonFile: File): JsonRecord = {
    val json = FileUtils.getTextFromFile(jsonFile)
    val jValue = JsonMethods.parse(json)
    val url = (jValue \ "url").extract[String]
    val titleOpt = (jValue \ "title").extractOpt[String]
    val datelineOpt = (jValue \ "dateline").extractOpt[String]
    val bylineOpt = (jValue \ "byline").extractOpt[String]
    val text = (jValue \ "text").extract[String]

    // Don't use them all in order to save space.
    JsonRecord(url, None, None, None, "")
  }

  def getDatasetUrls(): Set[String] = {
    // TODO: Also get terms from here instead of from directory names.
    val datasetUrls = Using.resource(Sourcer.sourceFromFilename(datasetFilename)) { source =>
      val tsvReader = new TsvReader()
      val datasetUrls = source.getLines.drop(1).map { line =>
        val Array(url) = tsvReader.readln(line, 1)

        url
      }.toSet

      datasetUrls
    }

    datasetUrls
  }

  def getJsonUrls(): Set[String] = {
    val jsonUrls = {
      val loneJsonFiles = new File(jsonDirname).listFilesByWildcard("*.json", recursive = true).toVector
      val pairedJsonFiles = loneJsonFiles.filter { jsonFile =>
        jsonFileToJsonld(jsonFile).exists
      }
      val jsonUrls = pairedJsonFiles.map { jsonFile =>
        val record = jsonFileToRecord(jsonFile)

        record.url
      }

      val set = jsonUrls.toSet
      val vectorLength = jsonUrls.length
      val setLength = set.size

      set
    }

    jsonUrls
  }

  val datasetUrls = getDatasetUrls()
  val jsonUrls = getJsonUrls()

  val missingJson = datasetUrls -- jsonUrls
  println("Missing jsons:")
  missingJson.foreach(println)

  val missingDataset = jsonUrls -- datasetUrls
  println()
  println("Missing dataset:")
  missingDataset.foreach(println)
}
