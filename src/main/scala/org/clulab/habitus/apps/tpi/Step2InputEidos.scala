package org.clulab.habitus.apps.tpi

import ai.lum.common.FileUtils._
import org.clulab.odin.{EventMention, Mention}
import org.clulab.utils.StringUtils
import org.clulab.wm.eidos.attachments.{Decrease, Increase, NegChange, Negation, PosChange}
import org.clulab.wm.eidos.document.AnnotatedDocument
import org.clulab.wm.eidos.serialization.jsonld.{JLDDeserializer, JLDRelationCausation}
import org.clulab.wm.eidoscommon.utils.{FileEditor, FileUtils, Logging, TsvWriter}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.io.File
import scala.util.Using

// See ArticleScrape
case class JsonRecord(url: String, titleOpt: Option[String], datelineOpt: Option[String], bylineOpt: Option[String], text: String)

case class AttributeCounts(increaseCount: Int, decreaseCount: Int, posChangeCount: Int, negChangeCount: Int, negatedCount: Int)

object Step2InputEidos extends App with Logging {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  val baseDirectory = "../corpora/multi"
  val outputFileName = "../corpora/multi/output.tsv"
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

  def mentionToAttributeCounts(mention: Mention): AttributeCounts = {
    val increaseCount = mention.attachments.count(_.isInstanceOf[Increase])
    val decreaseCount = mention.attachments.count(_.isInstanceOf[Decrease])
    val posCount = mention.attachments.count(_.isInstanceOf[PosChange])
    val negCount = mention.attachments.count(_.isInstanceOf[NegChange])
    val negationCount = mention.attachments.count(_.isInstanceOf[Negation])

    AttributeCounts(increaseCount, decreaseCount, posCount, negCount, negationCount)
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
        // .take(10000)
    val jsonldFiles = new File(baseDirectory).listFilesByWildcard("*.jsonld", recursive = true).toSet

    allJsonFiles.filter { jsonFile => jsonldFiles(jsonFileToJsonld(jsonFile)) }
  }.toSeq
  val jsonFileRecordPairs: Seq[(File, JsonRecord)] = jsonFiles.map { jsonFile =>
    (jsonFile, jsonFileToRecord(jsonFile))
  }
  val jsonFileRecordPairGroups: Seq[(String, Seq[(File, JsonRecord)])] = jsonFileRecordPairs.groupBy { jsonFileRecordPair => jsonFileRecordPair._2.url}.toSeq
  val jsonFileRecordTermsSeq: Seq[(File, JsonRecord, Seq[String])] = jsonFileRecordPairGroups.map { case (url, jsonFileRecordPairs) =>
    // Use just one jsonFile and jsonRecord, but combine search terms
    val terms = jsonFileRecordPairs.map { jsonFileRecordPair => jsonFileToTerm(jsonFileRecordPair._1) }

    (jsonFileRecordPairs.head._1, jsonFileRecordPairs.head._2, terms)
  }

  Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
    val tsvWriter = new TsvWriter(printWriter)

    tsvWriter.println(
      "url", "terms", "date", "sentenceIndex", "sentence",
      "causal", "causalIndex", "negationCount",
      "causeIncCount", "causeDecCount", "causePosCount", "causeNegCount",
      "effectIncCount", "effectDecCount", "effectPosCount", "effectNegCount",
      "causeText", "effectText"
    )
    jsonFileRecordTermsSeq.foreach { case (jsonFile, jsonRecord, terms) =>
      println(jsonFile.getPath)
      try {
        val url = jsonRecord.url
        val termsString = terms.mkString(" ")
        val jsonldFile = jsonFileToJsonld(jsonFile)
        val annotatedDocument = jsonldFileToAnnotatedDocument(jsonldFile)
        val document = annotatedDocument.document
        val documentText = document.text.getOrElse("")
        val allMentions = annotatedDocument.allOdinMentions
        val causalMentions = allMentions.filter { mention =>
          mention.isInstanceOf[EventMention] &&
          mention.label == JLDRelationCausation.taxonomy
        }
        val causalMentionGroups: Map[Int, Seq[Mention]] = causalMentions.groupBy(_.sentence)
        val sentences = document.sentences

        sentences.zipWithIndex.foreach { case (sentence, sentenceIndex) =>
          val causal = causalMentionGroups.contains(sentenceIndex)
          val rawText = documentText.slice(sentence.startOffsets.head, sentence.endOffsets.last)
          val cleanText = rawTextToCleanText(rawText)

          if (causal) {
            val causalMentionGroup = causalMentionGroups(sentenceIndex)

            causalMentionGroup.zipWithIndex.foreach { case (causalMention, causalIndex) =>
              val causalAttributeCounts = mentionToAttributeCounts(causalMention)
              assert(causalAttributeCounts.increaseCount == 0)
              assert(causalAttributeCounts.decreaseCount == 0)
              assert(causalAttributeCounts.posChangeCount == 0)
              assert(causalAttributeCounts.negChangeCount == 0)
              // Negation seems to be long up here.

              val causeMentions = causalMention.arguments("cause")
              assert(causeMentions.length == 1)
              val effectMentions = causalMention.arguments("effect")
              assert(effectMentions.length == 1)

              val causeMention = causeMentions.head
              val effectMention = effectMentions.head

              val causeText = causeMention.text
              val effectText = effectMention.text

              val causeAttributeCounts = mentionToAttributeCounts(causeMention)
              assert(causeAttributeCounts.negatedCount == 0)
              val effectAttributeCounts = mentionToAttributeCounts(effectMention)
              assert(effectAttributeCounts.negatedCount == 0)

              tsvWriter.print(url, termsString, jsonRecord.datelineOpt.getOrElse(""), sentenceIndex.toString, cleanText, "")
              tsvWriter.print(causal.toString, causalIndex.toString, causalAttributeCounts.negatedCount.toString, "")
              attributeCountsToTsvWriter(causeAttributeCounts, tsvWriter)
              attributeCountsToTsvWriter(effectAttributeCounts, tsvWriter)
              tsvWriter.println(causeText, effectText)
            }
          }
          else {
            tsvWriter.print(url, termsString, jsonRecord.datelineOpt.getOrElse(""), sentenceIndex.toString, cleanText, "")
            tsvWriter.print(causal.toString, "", "", "")
            tsvWriter.print( "", "", "", "", "")
            tsvWriter.print( "", "", "", "", "")
            tsvWriter.println( "", "")
          }
        }
      }
      catch {
        case throwable: Throwable =>
          logger.error(s"Exception for file $jsonFile", throwable)
      }
    }
  }
}
