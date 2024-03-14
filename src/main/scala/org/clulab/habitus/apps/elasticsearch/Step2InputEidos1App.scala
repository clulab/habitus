package org.clulab.habitus.apps.elasticsearch

import ai.lum.common.FileUtils._
import org.clulab.habitus.apps.utils.{AttributeCounts, JsonRecord}
import org.clulab.processors.{Document, Sentence}
import org.clulab.utils.StringUtils
import org.clulab.wm.eidos.document.AnnotatedDocument
import org.clulab.wm.eidos.serialization.jsonld.JLDDeserializer
import org.clulab.wm.eidoscommon.utils.{FileEditor, FileUtils, Logging, TsvWriter}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.io.File
import scala.util.Using

object Step2InputEidos1App extends App with Logging {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  val contextWindow = 3
  val baseDirectory = "../corpora/uganda-local"
  val outputFileName = "../corpora/uganda-local/uganda.tsv"
  val deserializer = new JLDDeserializer()

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
    JsonRecord(url, None, datelineOpt, None, "")
  }

  def jsonFileToTerm(jsonFile: File): String = {
    val path = jsonFile.getPath
    val term = StringUtils.afterLast(
      StringUtils.beforeLast(
        StringUtils.beforeLast(
          StringUtils.beforeLast(
            path, '/'
          ), '/'
        ), '/'
      ), '/'
    )

    term
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

  val jsonFiles: Seq[File] = {
    val allJsonFiles = new File(baseDirectory).listFilesByWildcard("*.json", recursive = true)
        // TODO remove take
        // .take(1000)
    val jsonldFiles = new File(baseDirectory).listFilesByWildcard("*.jsonld", recursive = true).toSet

    allJsonFiles.filter { jsonFile => jsonldFiles(jsonFileToJsonld(jsonFile)) }
  }.toSeq.sorted
  val jsonFileRecordPairs: Seq[(File, JsonRecord)] = jsonFiles.map { jsonFile =>
    (jsonFile, jsonFileToRecord(jsonFile))
  }
  val jsonFileRecordPairGroups: Seq[(String, Seq[(File, JsonRecord)])] = jsonFileRecordPairs.groupBy { jsonFileRecordPair => jsonFileRecordPair._2.url}.toSeq
  val jsonFileRecordTermsSeq: Seq[(File, JsonRecord, Seq[String])] = jsonFileRecordPairGroups.map { case (_, jsonFileRecordPairs) =>
    // Use just one jsonFile and jsonRecord, but combine search terms
    val terms = jsonFileRecordPairs.map { jsonFileRecordPair => jsonFileToTerm(jsonFileRecordPair._1) }

    (jsonFileRecordPairs.head._1, jsonFileRecordPairs.head._2, terms)
  }

  Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
    val tsvWriter = new TsvWriter(printWriter)

    tsvWriter.println("url", "sentenceIndex", "sentence", "context", "prevSentence")
    jsonFileRecordTermsSeq.zipWithIndex.foreach { case ((jsonFile, jsonRecord, terms), index) =>
      println(s"$index ${jsonFile.getPath}")
      try {
        val url = jsonRecord.url
        val jsonldFile = jsonFileToJsonld(jsonFile)
        val annotatedDocument = jsonldFileToAnnotatedDocument(jsonldFile)
        val document = annotatedDocument.document
        val sentences = document.sentences

        sentences.zipWithIndex.foreach { case (sentence, sentenceIndex) =>
          val cleanText = getSentenceText(document, sentence)
          val context = sentences
              .slice(sentenceIndex - contextWindow, sentenceIndex + contextWindow + 1)
              .map(getSentenceText(document,_))
              .mkString(" ")
          val prevSentenceText = sentences
              .lift(sentenceIndex - 1)
              .map(getSentenceText(document, _))
              .getOrElse("")

          tsvWriter.println(url, sentenceIndex.toString, cleanText, context, prevSentenceText)
        }
      }
      catch {
        case throwable: Throwable =>
          logger.error(s"Exception for file $jsonFile", throwable)
      }
    }
  }
}
