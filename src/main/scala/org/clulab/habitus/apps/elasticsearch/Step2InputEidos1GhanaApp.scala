package org.clulab.habitus.apps.elasticsearch

import ai.lum.common.FileUtils._
import org.clulab.habitus.apps.elasticsearch.VerifyGhanaApp.datasetFilename
import org.clulab.habitus.apps.utils.{AttributeCounts, JsonRecord}
import org.clulab.processors.{Document, Sentence}
import org.clulab.utils.{Sourcer, StringUtils}
import org.clulab.wm.eidos.document.AnnotatedDocument
import org.clulab.wm.eidos.serialization.jsonld.JLDDeserializer
import org.clulab.wm.eidoscommon.utils.{FileEditor, FileUtils, Logging, TsvReader, TsvWriter}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.io.File
import scala.util.Using

object Step2InputEidos1GhanaApp extends App with Logging {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  val contextWindow = 3
  val datasetFilename = "../corpora/ghana-elasticsearch/dataset55k.tsv"
  val baseDirectory = "/home/kwa/data/Corpora/habitus-project/corpora/multimix"
  val outputFileName = "../corpora/ghana-elasticsearch/ghana-elasticsearch.tsv"
  val deserializer = new JLDDeserializer()

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

  def jsonldFileToAnnotatedDocument(jsonldFile: File): AnnotatedDocument = {
    val json = FileUtils.getTextFromFile(jsonldFile)
    val corpus = deserializer.deserialize(json)
    val annotatedDocument = corpus.head

    annotatedDocument
  }

  def rawTextToCleanText(rawText: String): String = rawText
      .trim
      .replaceAll("\r\n", " ")
      .replaceAll("\n", " ")
      .replaceAll("\r", " ")
      .replaceAll("\t", " ")
      .replaceAll("\u2028", " ") // unicode line separator
      .replaceAll("\u2029", " ") // unicode paragraph separator
      .map { letter =>
        if (letter.toInt < 32) ' '
        else letter
      }
      .trim

  def getSentenceText(document: Document, sentence: Sentence): String = {
    val rawText = document.text.get.slice(sentence.startOffsets.head, sentence.endOffsets.last)
    val cleanText = rawTextToCleanText(rawText)

    cleanText
  }

  def attributeCountsToTsvWriter(attributeCounts: AttributeCounts, tsvWriter: TsvWriter): Unit = {
    tsvWriter.print(
      attributeCounts.increaseCount.toString, attributeCounts.decreaseCount.toString,
      attributeCounts.posChangeCount.toString, attributeCounts.negChangeCount.toString,
      ""
    )
  }

  val datasetUrls: Set[String] = getDatasetUrls
  val jsonFilesAndUrls: Seq[(File, String)] = {
    val allJsonFiles = new File(baseDirectory).listFilesByWildcard("*.json", recursive = true).toVector
    val jsonFilesWithJsonld = allJsonFiles.filter { jsonFile =>
      jsonFileToJsonld(jsonFile).exists
    }
    val jsonFilesAndUrls: Seq[(File, String)] = jsonFilesWithJsonld.map { jsonFile =>
      val record = jsonFileToRecord(jsonFile)

      (jsonFile, record.url)
    }
    val headJsonFilesAndUrls = jsonFilesAndUrls.groupBy(_._2).map(_._2.head).toSeq

    headJsonFilesAndUrls
  }

  Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
    val tsvWriter = new TsvWriter(printWriter)

    tsvWriter.println("url", "sentenceIndex", "sentence", "context", "prevSentence")
    datasetUrls.zipWithIndex.foreach { case (url, index) =>
      val jsonFile = jsonFilesAndUrls.find(_._2 == url).get._1

      println(s"$index ${jsonFile.getPath}")
      try {
        val jsonldFile = jsonFileToJsonld(jsonFile)
        val annotatedDocument = jsonldFileToAnnotatedDocument(jsonldFile)
        val document = annotatedDocument.document
        val sentences = document.sentences

        sentences.zipWithIndex.foreach { case (sentence, sentenceIndex) =>
          val cleanText = getSentenceText(document, sentence)
          val context = sentences
              .slice(sentenceIndex - contextWindow, sentenceIndex + contextWindow + 1)
              .map(getSentenceText(document, _))
              .mkString(" ")
          val prevSentenceText = sentences
              .lift(sentenceIndex - 1)
              .map(getSentenceText(document, _))
              .getOrElse("")

          tsvWriter.println(url, sentenceIndex.toString, cleanText, context, prevSentenceText)
        }
      }
      catch
      {
        case throwable: Throwable =>
          logger.error(s"Exception for file $jsonFile", throwable)
      }
    }
  }
}
