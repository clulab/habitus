package org.clulab.habitus.apps.elasticsearch

import ai.lum.common.FileUtils._
import org.clulab.habitus.apps.tpi.AttributeCounts
import org.clulab.habitus.apps.utils.DateString
import org.clulab.habitus.elasticsearch.ElasticsearchIndexClient
import org.clulab.habitus.elasticsearch.data.{CausalRelation, CauseOrEffect, DatasetRecord, LatLon, Location, Relation}
import org.clulab.habitus.elasticsearch.utils.Elasticsearch
import org.clulab.odin.{EventMention, Mention}
import org.clulab.processors.{Document, Sentence}
import org.clulab.utils.{Sourcer, StringUtils}
import org.clulab.wm.eidos.attachments.{Decrease, Increase, NegChange, Negation, PosChange}
import org.clulab.wm.eidos.document.AnnotatedDocument
import org.clulab.wm.eidos.serialization.jsonld.{JLDDeserializer, JLDRelationCausation}
import org.clulab.wm.eidoscommon.utils.{FileEditor, FileUtils, Logging, TsvReader, TsvWriter}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.io.File
import java.net.URL
import scala.util.Using

// See ArticleScrape
case class JsonRecord(url: String, titleOpt: Option[String], datelineOpt: Option[String], bylineOpt: Option[String], text: String)

case class AttributeCounts(increaseCount: Int, decreaseCount: Int, posChangeCount: Int, negChangeCount: Int, negatedCount: Int)

case class TsvRecord(
  sentenceIndex: Int,
  sentence: String,
  belief: Boolean,
  sentimentScoreOpt: Option[Float],
  sentenceLocations: Array[Location],
  contextLocations: Array[Location]
)

object Step2InputEidos2 extends App with Logging {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  val contextWindow = 3
  val baseDirectory = "../corpora/uganda-mining"
  val inputFilename = "../corpora/uganda-mining/uganda-2.tsv"
  val credentialsFilename = "../credentials/credentials.properties"
  val deserializer = new JLDDeserializer()
  val url = new URL("http://localhost:9200")
  // val url = new URL("https://elasticsearch.keithalcock.com")
  val indexName = "habitus"
  val datasetName = "uganda-mining.tsv"
  val regionName = "uganda"

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

  def mentionToAttributeCounts(mention: Mention): AttributeCounts = {
    val increaseCount = mention.attachments.count(_.isInstanceOf[Increase])
    val decreaseCount = mention.attachments.count(_.isInstanceOf[Decrease])
    val posCount = mention.attachments.count(_.isInstanceOf[PosChange])
    val negCount = mention.attachments.count(_.isInstanceOf[NegChange])
    val negationCount = mention.attachments.count(_.isInstanceOf[Negation])

    AttributeCounts(increaseCount, decreaseCount, posCount, negCount, negationCount)
  }

  def newCauseOrEffect(text: String, attributeCounts: AttributeCounts): CauseOrEffect = {
    new CauseOrEffect(text,
      attributeCounts.increaseCount, attributeCounts.decreaseCount,
      attributeCounts.posChangeCount, attributeCounts.negChangeCount
    )
  }

  def parseLocations(locationString: String): Array[Location] = {
    if (locationString.isEmpty) Array.empty
    else {
      val locations = locationString.split(')').map { nameAndLatLon =>
        val Array(name, latLon) = nameAndLatLon.split('(').map(_.trim)
        val Array(lat, lon) = latLon.split(",").map(_.trim.toFloat)

        Location(name, LatLon(lat, lon))
      }

      locations
    }
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
  val urlSentenceIndexToTsvRecordMap = Using.resource(Sourcer.sourceFromFilename(inputFilename)) { source =>
    val lines = source.getLines.drop(1)
    val tsvReader = new TsvReader()

    lines.map { line =>
      val Array(url, sentenceIndexString, sentence, beliefString, sentimentScore, sentenceLocationsString, contextLocationsString) = tsvReader.readln(line, 7)
      val sentenceIndex = sentenceIndexString.toInt
      val belief = beliefString == "True"
      val sentimentScoreOpt = if (sentimentScore.isEmpty) None else Some(sentimentScore.toFloat)
      val sentenceLocations = parseLocations(sentenceLocationsString)
      val contextLocations = parseLocations(contextLocationsString)

      (url, sentenceIndex) -> new TsvRecord(sentenceIndex, sentence, belief, sentimentScoreOpt, sentenceLocations, contextLocations)
    }.toMap
  }

  Using.resource(ElasticsearchIndexClient(url, credentialsFilename, indexName)) { elasticsearchIndexClient =>
    jsonFileRecordTermsSeq.zipWithIndex.foreach { case ((jsonFile, jsonRecord, terms), index) =>
      println(s"$index ${jsonFile.getPath}")
      try {
        val url = jsonRecord.url
        val jsonldFile = jsonFileToJsonld(jsonFile)
        val annotatedDocument = jsonldFileToAnnotatedDocument(jsonldFile)
        val document = annotatedDocument.document
        val allMentions = annotatedDocument.allOdinMentions
        val causalMentions = allMentions.filter { mention =>
          mention.isInstanceOf[EventMention] &&
          mention.label == JLDRelationCausation.taxonomy
        }
        val causalMentionGroups: Map[Int, Seq[Mention]] = causalMentions.groupBy(_.sentence)
        val sentences = document.sentences
        val dateOpt = jsonRecord.datelineOpt.map(DateString(_).canonicalize)

        sentences.zipWithIndex.foreach { case (sentence, sentenceIndex) =>
          val causal = causalMentionGroups.contains(sentenceIndex)
          val cleanText = getSentenceText(document, sentence)
          val context = sentences
              .slice(sentenceIndex - contextWindow, sentenceIndex + contextWindow + 1)
              .map(getSentenceText(document,_))
              .mkString(" ")
          val prevSentenceText = sentences
              .lift(sentenceIndex - 1)
              .map(getSentenceText(document, _))
              .getOrElse("")
          val tsvRecord = urlSentenceIndexToTsvRecordMap(url, sentenceIndex)
          val contextBefore = sentences
              .slice(sentenceIndex - contextWindow, sentenceIndex)
              .map(getSentenceText(document,_))
              .mkString(" ")
          val contextAfter = sentences
              .slice(sentenceIndex + 1, sentenceIndex + contextWindow + 1)
              .map(getSentenceText(document,_))
              .mkString(" ")
          val prevLocationsIndexOpt = Range(0, sentenceIndex).reverse.find { sentenceIndex =>
            val tsvRecord = urlSentenceIndexToTsvRecordMap(url, sentenceIndex)

            tsvRecord.sentenceLocations.nonEmpty
          }
          val prevLocations = prevLocationsIndexOpt.map { prevLocationsIndex =>
            val tsvRecord = urlSentenceIndexToTsvRecordMap(url, prevLocationsIndex)

            tsvRecord.sentenceLocations
          }.getOrElse(Array.empty)
          val prevDistanceOpt = prevLocationsIndexOpt.map(sentenceIndex - _)
          val nextLocationsIndexOpt = Range(sentenceIndex + 1, document.sentences.length).find {sentenceIndex =>
            val tsvRecord = urlSentenceIndexToTsvRecordMap(url, sentenceIndex)

            tsvRecord.sentenceLocations.nonEmpty
          }
          val nextLocations = nextLocationsIndexOpt.map { nextLocationsIndex =>
            val tsvRecord = urlSentenceIndexToTsvRecordMap(url, nextLocationsIndex)

            tsvRecord.sentenceLocations
          }.getOrElse(Array.empty)
          val nextDistanceOpt = nextLocationsIndexOpt.map(sentenceIndex - _)

          val causalRelations = if (causal) {
            val causalMentionGroup = causalMentionGroups(sentenceIndex).sorted
            val causalRelations = causalMentionGroup.zipWithIndex.map { case (causalMention, causalIndex) =>
              val causalAttributeCounts = mentionToAttributeCounts(causalMention)
              assert(causalAttributeCounts.increaseCount == 0)
              assert(causalAttributeCounts.decreaseCount == 0)
              assert(causalAttributeCounts.posChangeCount == 0)
              assert(causalAttributeCounts.negChangeCount == 0)

              // TODO: Keith restart here!
              val causeMentions = causalMention.arguments("cause")
              assert(causeMentions.length == 1)
              val effectMentions = causalMention.arguments("effect")
              assert(effectMentions.length == 1)

              val causeMention = causeMentions.head
              val effectMention = effectMentions.head

              val causeText = causeMention.text
              val cleanCauseText = rawTextToCleanText(causeText)
              val effectText = effectMention.text
              val cleanEffectText = rawTextToCleanText(effectText)

              val causeAttributeCounts = mentionToAttributeCounts(causeMention)
              assert(causeAttributeCounts.negatedCount == 0)
              val effectAttributeCounts = mentionToAttributeCounts(effectMention)
              assert(effectAttributeCounts.negatedCount == 0)

              val cause = newCauseOrEffect(cleanCauseText, causeAttributeCounts)
              val effect = newCauseOrEffect(cleanEffectText, effectAttributeCounts)
              val relation = Relation(cause, effect)
              val causalRelation = CausalRelation(
                causalIndex,
                0, // negationCount,
                Array(relation)
              )

              Array(causalRelation)
            }

            causalRelations
          }
          else Array.empty[CausalRelation]

          val datasetRecord: DatasetRecord = DatasetRecord(
            datasetName,
            regionName,
            url,
            jsonRecord.titleOpt,
            terms.toArray,
            jsonRecord.datelineOpt,
            dateOpt,
            jsonRecord.bylineOpt,
            tsvRecord.sentenceIndex,
            tsvRecord.sentence,
            causalRelations,
            tsvRecord.belief,
            tsvRecord.sentimentScoreOpt,
            tsvRecord.sentenceLocations,
            contextBefore,
            contextAfter,
            tsvRecord.contextLocations,
            prevLocations,
            prevDistanceOpt,
            nextLocations,
            nextDistanceOpt
          )

            elasticsearchIndexClient.index(datasetRecord)
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
