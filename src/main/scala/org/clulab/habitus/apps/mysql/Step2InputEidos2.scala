package org.clulab.habitus.apps.mysql

import ai.lum.common.FileUtils._
import org.clulab.habitus.apps.utils.{AttributeCounts, DateString, JsonRecord}
import org.clulab.habitus.elasticsearch.data.{CausalRelation, CauseOrEffect, DatasetRecord, LatLon, Location, Relation}
import org.clulab.habitus.mysql.apps.utils.Credentials
import org.clulab.odin.{EventMention, Mention}
import org.clulab.processors.{Document, Sentence}
import org.clulab.utils.{Sourcer, StringUtils}
import org.clulab.wm.eidos.attachments.{Decrease, Increase, NegChange, Negation, PosChange}
import org.clulab.wm.eidos.document.AnnotatedDocument
import org.clulab.wm.eidos.serialization.jsonld.{JLDDeserializer, JLDRelationCausation}
import org.clulab.wm.eidoscommon.utils._
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.io.File
import java.sql.{Connection, Date, DriverManager, Statement, Timestamp}
import java.time.LocalDateTime
import scala.util.{Try, Using}

object Step2InputEidos2 extends App with Logging {

  case class LocalTsvRecord(
    sentenceIndex: Int,
    sentence: String,
    belief: Boolean,
    sentimentScoreOpt: Option[Float],
    sentenceLocations: Array[Location],
    contextLocations: Array[Location]
  )

  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  val contextWindow = 3
  val baseDirectory = "../corpora/uganda-mining"
  val inputFilename = "../corpora/uganda-mining/uganda-2.tsv"
  val credentialsFilename = "../credentials/mysql-credentials.properties"
  val deserializer = new JLDDeserializer()
  val indexName = "habitus"
  val url = s"jdbc:mysql://localhost:3306/$indexName?serverTimezone=UTC"
  val datasetName = "uganda-mining.tsv"
  val regionName = "uganda"
  val credentials = new Credentials(credentialsFilename)

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
    JsonRecord(url, titleOpt, datelineOpt, bylineOpt, "")
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
        val Array(latString, lonString) = latLon.split(",").map(_.trim)
        // Sometimes we have NaN for these.  We know it's a place, but not where.
        val latOpt = Try(latString.toFloat).toOption
        val lonOpt = Try(lonString.toFloat).toOption
        val latLonOpt = latOpt.flatMap { lat =>
          lonOpt.map { lon =>
            LatLon(lat, lon)
          }
        }

        Location(name,latLonOpt)
      }

      locations
    }
  }

  def getCausalRelations(causalMentionGroup: Seq[Mention]): Array[CausalRelation] = {
    val causalRelations = causalMentionGroup.zipWithIndex.map { case (causalMention, causalIndex) =>
      val causalAttributeCounts = mentionToAttributeCounts(causalMention)
      assert(causalAttributeCounts.increaseCount == 0)
      assert(causalAttributeCounts.decreaseCount == 0)
      assert(causalAttributeCounts.posChangeCount == 0)
      assert(causalAttributeCounts.negChangeCount == 0)

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
        causalAttributeCounts.negatedCount,
        Array(relation)
      )

      causalRelation
    }

    causalRelations.toArray
  }

  def runIndex(connection: Connection, datasetRecord: DatasetRecord): Unit = {
    try {
      val datasetId = {
        val preparedStatement = {
          val preparedStatement = connection.prepareStatement("SELECT id FROM dataset WHERE name = ?")

          preparedStatement.setString(1, datasetName)
          preparedStatement
        }
        val resultSet = preparedStatement.executeQuery()
        val datasetId =
          if (resultSet.next()) resultSet.getInt(1)
          else throw new RuntimeException("Couldn't get regionId!")

        datasetId
      }
      val documentId = {
        val preparedStatement = {
          val preparedStatement = connection.prepareStatement(
            "INSERT IGNORE INTO document (datasetId, url, title, dateline, byline, date) " +
            "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
          )
          val localDateTimeOpt = datasetRecord.dateOpt.map(LocalDateTime.parse)
          val timestampOpt = localDateTimeOpt.map(Timestamp.valueOf)

          preparedStatement.setInt(1, datasetId)
          preparedStatement.setString(2, datasetRecord.url)
          preparedStatement.setString(3, datasetRecord.titleOpt.orNull)
          preparedStatement.setString(4, datasetRecord.datelineOpt.orNull)
          preparedStatement.setString(5, datasetRecord.bylineOpt.orNull)
          preparedStatement.setTimestamp(6, timestampOpt.orNull)
          preparedStatement
        }
        preparedStatement.execute()
        val resultSet = preparedStatement.getGeneratedKeys()
        val documentId =
          if (resultSet.next) resultSet.getInt(1)
          else throw new RuntimeException("Couldn't get documentId!")

        documentId
      }
      datasetRecord.terms.foreach { term =>
        val termId = {
          val preparedStatement = {
            val preparedStatement = connection.prepareStatement(
              "SELECT id FROM term " +
              "WHERE name = ?"
            )

            preparedStatement.setString(1, term)
            preparedStatement
          }

          val resultSet = preparedStatement.executeQuery()
          val termId =
            if (resultSet.next) resultSet.getInt(1)
            else throw new RuntimeException("Couldn't get termId!")

          termId
        }
        val preparedStatement = {
          val preparedStatement = connection.prepareStatement(
            "INSERT INTO documentTerms (documentId, termId) " +
            "VALUES (?, ?)"
          )

          preparedStatement.setInt(1, documentId)
          preparedStatement.setInt(2, termId)
          preparedStatement
        }

        preparedStatement.execute
      }

      connection.commit()
      // Now handle sentence-level things
    }
    catch {
      case throwable: Throwable => connection.rollback()
    }
  }

  def getLocationsAndDistance(sentenceIndex: Int, range: Range, urlSentenceIndexToTsvRecordMap: Map[(String, Int),
      LocalTsvRecord], url: String): (Array[Location], Option[Int]) = {
    val tsvRecord = urlSentenceIndexToTsvRecordMap(url, sentenceIndex)

    if (tsvRecord.sentenceLocations.nonEmpty)
      (tsvRecord.sentenceLocations, Some(0))
    else {
      val locationsIndexOpt = range.find { sentenceIndex =>
        urlSentenceIndexToTsvRecordMap(url, sentenceIndex).sentenceLocations.nonEmpty
      }
      val locations = locationsIndexOpt.map { sentenceIndex =>
        urlSentenceIndexToTsvRecordMap(url, sentenceIndex).sentenceLocations
      }.getOrElse(Array.empty)
      val distanceOpt = locationsIndexOpt.map { index => math.abs(sentenceIndex - index) }

      (locations, distanceOpt)
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
  val urlSentenceIndexToTsvRecordMap: Map[(String, Int), LocalTsvRecord] = Using.resource(Sourcer.sourceFromFilename(inputFilename)) { source =>
    val lines = source.getLines.drop(1)
    val tsvReader = new TsvReader()

    lines.map { line =>
      val Array(url, sentenceIndexString, sentence, beliefString, sentimentScore, sentenceLocationsString, contextLocationsString) = tsvReader.readln(line, 7)
      val sentenceIndex = sentenceIndexString.toInt
      val belief = beliefString == "True"
      val sentimentScoreOpt = if (sentimentScore.isEmpty) None else Some(sentimentScore.toFloat)
      val sentenceLocations = parseLocations(sentenceLocationsString)
      val contextLocations = parseLocations(contextLocationsString)

      (url, sentenceIndex) -> new LocalTsvRecord(sentenceIndex, sentence, belief, sentimentScoreOpt, sentenceLocations, contextLocations)
    }.toMap
  }
  val connection: Connection = {
    val connection = DriverManager.getConnection(url, credentials.username, credentials.password)

    connection.setAutoCommit(false)
    connection
  }

  Using.resource(connection) { connection =>
    jsonFileRecordTermsSeq.zipWithIndex.foreach { case ((jsonFile, jsonRecord, terms), index) =>
      println(s"$index ${jsonFile.getPath}")
      try {
        val url = jsonRecord.url
        val jsonldFile = jsonFileToJsonld(jsonFile)
        val annotatedDocument = jsonldFileToAnnotatedDocument(jsonldFile)
        val document = annotatedDocument.document
        val allMentions = annotatedDocument.allOdinMentions.toVector
        val causalMentions = allMentions.filter { mention =>
          mention.isInstanceOf[EventMention] && mention.label == JLDRelationCausation.taxonomy
        }
        val causalMentionGroups: Map[Int, Seq[Mention]] = causalMentions.groupBy(_.sentence)
        val sentences = document.sentences
        val dateOpt = jsonRecord.datelineOpt.map(DateString(_).canonicalize)

        sentences.zipWithIndex.foreach { case (sentence, sentenceIndex) =>
          val causal = causalMentionGroups.contains(sentenceIndex)
          val cleanText = getSentenceText(document, sentence)
          val tsvRecord = urlSentenceIndexToTsvRecordMap(url, sentenceIndex)
          val contextBefore = sentences
              .slice(sentenceIndex - contextWindow, sentenceIndex)
              .map(getSentenceText(document, _))
              .mkString(" ")
          val contextAfter = sentences
              .slice(sentenceIndex + 1, sentenceIndex + contextWindow + 1)
              .map(getSentenceText(document, _))
              .mkString(" ")
          val (prevLocations, prevDistanceOpt) = getLocationsAndDistance(sentenceIndex,
              Range(0, sentenceIndex).reverse, urlSentenceIndexToTsvRecordMap, url)
          val (nextLocations, nextDistanceOpt) = getLocationsAndDistance(sentenceIndex,
              Range(sentenceIndex + 1, document.sentences.length), urlSentenceIndexToTsvRecordMap, url)
          val causalRelations =
              if (causal) getCausalRelations(causalMentionGroups(sentenceIndex).sorted)
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
            cleanText,
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

          runIndex(connection, datasetRecord)
        }
      }
      catch {
        case throwable: Throwable =>
          logger.error(s"Exception for file $jsonFile", throwable)
      }
    }
  }
}
