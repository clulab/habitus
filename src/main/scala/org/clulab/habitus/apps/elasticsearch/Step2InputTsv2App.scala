package org.clulab.habitus.apps.elasticsearch

import org.clulab.habitus.apps.utils.JsonRecord
import org.clulab.habitus.elasticsearch.ElasticsearchIndexClient
import org.clulab.habitus.elasticsearch.data._
import org.clulab.habitus.elasticsearch.utils.Elasticsearch
import org.clulab.habitus.utils.TsvReader
import org.clulab.utils.Sourcer
import org.clulab.wm.eidoscommon.utils.{FileUtils, Logging}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods
import zamblauskas.csv.parser._
import zamblauskas.functional._

import java.io.File
import java.net.URL
import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer
import scala.util.{Try, Using}

// TODO: This has been abandoned!

object Step2InputTsv2App extends App with Logging {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  val baseFilename = "3news_com"
  val jsonDirname = ""
  val baseDirname = "/home/kwa/data/Corpora/habitus-project/corpora/ghana-sitemap/zipping"
  val inputDataFilename = s"$baseDirname/$baseFilename-c.tsv" // This has the dates and distances.
  val inputContextFilename = s"$baseDirname/$baseFilename.tsv" // This one has the context.
  val inputVectorFilename = s"$baseDirname/$baseFilename-d.tsv" // This has the vectors.
  val credentialsFilename = "../credentials/elasticsearch-credentials.properties"
  val url = new URL("http://localhost:9200")
  // val url = new URL("https://elasticsearch.keithalcock.com")
  val indexName = "habitus6"
  val datasetName = "ghana-sitemap.tsv"
  val regionName = "ghana"
  val alreadyNormalized = true
  val tsvReader = new TsvReader()
  val cleaner = new Cleaner()

  class Cleaner {

    def clean(name: String): String = {
      name.map { char =>
        if (char.isLetterOrDigit) char else '_'
      }
    }
  }

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

  def getJsonRecord(url: String): JsonRecord = {
    val cleanFile = cleaner.clean(new URL(url).getFile)
    val filename = s"$jsonDirname/$cleanFile.json"
    val jsonFile = new File(filename)
    val jsonRecord = jsonFileToRecord(jsonFile)

    jsonRecord
  }

  case class Key(url: String, sentenceIndex: String, sentence: String)

  trait Keyed {
    def getKey: Key
  }

  case class DataRecord(
    url: String,
    sentenceIndex: String,
    sentence: String,

    terms: String,
    date: String,
    causal: String,
    causalIndex: String,
    negationCount: String,
    causeIncCount: String,
    causeDecCount: String,
    causePosCount: String,
    causeNegCount: String,
    effectIncCount: String,
    effectDecCount: String,
    effectPosCount: String,
    effectNegCount: String,
    causeText: String,
    effectText: String,
    belief: String,
    sentiment_scores: String,
    sent_locs: String,
    context_locs: String,
    canonicalDate: String,
    prevLocation: String,
    prevDistance: String,
    nextLocation: String,
    nextDistance: String
  ) extends Keyed {

    def getKey: Key = Key(url, sentenceIndex, sentence)

    def getTerms: Array[String] = {
      terms.split(",").map(_.trim)
    }

    def getCausalRelationOpt: Option[CausalRelation] = {
      if (causalIndex.nonEmpty) {
        val cause = CauseOrEffect(causeText, causeIncCount.toInt, causeDecCount.toInt, causePosCount.toInt, causeNegCount.toInt)
        val effect = CauseOrEffect(effectText, effectIncCount.toInt, effectDecCount.toInt, effectPosCount.toInt, effectNegCount.toInt)
        val causalRelation = CausalRelation(causalIndex.toInt, negationCount.toInt, cause, effect)

        Some(causalRelation)
      }
      else None
    }

    def parseLocations(locationString: String): Array[Location] = {
      if (locationString.isEmpty) Array.empty
      else {
        val locations = locationString.split(')').map { commaAndNameAndLatLon =>
          val trimmedCommaAndNameAndLatLon = commaAndNameAndLatLon.trim
          val toDrop = if (trimmedCommaAndNameAndLatLon.startsWith(",")) 1 else 0
          val nameAndLatLon = trimmedCommaAndNameAndLatLon.drop(toDrop)
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

    def getLocations: Array[Location] = parseLocations(sent_locs).toArray

    def getContextLocations: Seq[Location] = parseLocations(context_locs)

    def getBelief: Boolean = (belief.toLowerCase == "true")

    def getSentimentScoreOpt: Option[Float] = {
      if (sentiment_scores.nonEmpty) Some(sentiment_scores.toFloat)
      else None
    }
  }

//  implicit val dataRecordReads: ColumnReads[DataRecord] = (
//     TODO: Are all columns needed?
//    column("url").as[String] and
//    column("sentenceIndex").as[String] and
//    column("sentence").as[String] and
//    column("terms").as[String] and
//    column("date").as[String] and
//    column("causal").as[String] and
//    column("causalIndex").as[String] and
//    column("negationCount").as[String] and
//    column("causeIncCount").as[String] and
//    column("causeDecCount").as[String] and
//    column("causePosCount").as[String] and
//    column("causeNegCount").as[String] and
//    column("effectIncCount").as[String] and
//    column("effectDecCount").as[String] and
//    column("effectPosCount").as[String] and
//    column("effectNegCount").as[String] and
//    column("belief").as[String] and
//    column("sentiment_scores").as[String] and
//    column("sent_locs").as[String] and
//    column("context_locs").as[String] and
//    column("canonicalDate").as[String] and
//    column("prevLocation").as[String] and
//    column("prevDistance").as[String] and
//    column("nextLocation").as[String] and
//    column("nextDistance").as[String]
//  )(DataRecord)

  case class ContextRecord(
    url: String,
    sentenceIndex: String,
    sentence: String,

    context: String
  ) extends Keyed {

    def getKey: Key = Key(url, sentenceIndex, sentence)
  }

  implicit val contextRecordReads: ColumnReads[ContextRecord] = (
    // TODO: Are all columns needed?
    column("url").as[String] and
    column("sentenceIndex").as[String] and
    column("sentence").as[String] and
    column("context").as[String]
  )(ContextRecord)

  case class VectorRecord(
    url: String, // double check
    sentenceIndex: String,
    sentence: String,

    vector: String
  ) extends Keyed {

    def getKey: Key = Key(url, sentenceIndex, sentence)

    def parseVector(vectorString: String): Array[Float] = {
      val values = vectorString.split(", ")
      val floats = values.map(_.toFloat)

      floats
    }

    def normalize(floats: Array[Float]): Array[Float] = {
      if (alreadyNormalized) floats
      else {
        val sumSquare = floats.foldLeft(0f) { case (sum, float) => sum + float * float }
        val divisor = math.sqrt(sumSquare)
        val normalized = floats.map { float => (float / divisor).toFloat }

        normalized
      }
    }

    def getVector: Array[Float] = normalize(parseVector(vector))
  }

  implicit val vectorRecordReads: ColumnReads[VectorRecord] = (
    column("url").as[String] and
    column("sentenceIndex").as[String] and
    column("sentence").as[String] and
    column("vector").as[String]
  )(VectorRecord)

  def unescape(string: String): String = tsvReader.unescape(string)

  case class SentenceData(
    headDataRecord: DataRecord, headContextRecord: ContextRecord, headVectorRecord: VectorRecord,
    tailDataRecords: Seq[DataRecord]
  ) {
    val jsonRecord = getJsonRecord(headDataRecord.url)
    val causalRelations = (headDataRecord.getCausalRelationOpt +: tailDataRecords.map(_.getCausalRelationOpt)).flatten.toArray

    {
      val key = headDataRecord.getKey

      assert(key == headContextRecord.getKey)
      assert(key == headVectorRecord.getKey)
      tailDataRecords.foreach { tailDataRecord =>
        assert(key == tailDataRecord.getKey)
      }
    }

    def toDatasetRecord: Unit = {
      try {
        tryToDatasetRecord
      }
      catch {
        case throwable: Throwable =>
          logger.error(s"Exception for record $headDataRecord", throwable)
      }
    }

    def option(string: String): Option[String] =
        if (string.isEmpty) None
        else Some(string)

    def tryToDatasetRecord: Unit = {
      val datasetRecord: DatasetRecord = DatasetRecord(
        datasetName,
        regionName,
        headDataRecord.url,
        jsonRecord.titleOpt,
        headDataRecord.getTerms,
        option(headDataRecord.date),
        option(headDataRecord.canonicalDate),
        jsonRecord.bylineOpt,
        headDataRecord.sentenceIndex.toInt,
        headDataRecord.sentence,
        causalRelations,
        headDataRecord.getBelief,
        headDataRecord.getSentimentScoreOpt,
        headDataRecord.getLocations,
        null, // headDataRecord.contextBefore, // do need a for the context
        null, // headDataRecord.contextAfter, // but that doesn't have what is in b and c
        null, // contextLocations,
        null, // prevLocations,
        null, // prevDistanceOpt,
        null, // headDataRecord.nextLocation,
        null, // headDataRecord.nextDistance,
        null // headVectorRecord.getVector
      )
    }
  }

  // I have arranged it so that everything is on a single line.  So the csv records are all like that.
  def getSentenceDataOpt(
    dataLines: BufferedIterator[String],
    contextLines: BufferedIterator[String],
    vectorLines: BufferedIterator[String],
    dataHeader: String, contextHeader: String, vectorHeader: String // The headers may be needed for columns
  ): Option[SentenceData] = {
    if (!dataLines.hasNext) {
      assert(!contextLines.hasNext)
      assert(!vectorLines.hasNext)
      None
    }
    else {
      val headDataRecord: DataRecord = null // Parser.parse[DataRecord](unescape(dataLines.next)).toOption.get.head
      val headContextRecord = Parser.parse[ContextRecord](unescape(contextLines.next)).toOption.get.head
      val headVectorRecord = Parser.parse[VectorRecord](unescape(vectorLines.next)).toOption.get.head
      val dataRecords = new ArrayBuffer[DataRecord]()
      val key = headDataRecord.getKey

      @tailrec
      def readRecords(): Unit = {
        if (dataLines.hasNext) {
          val headTail: DataRecord = null // Parser.parse[DataRecord](unescape(dataLines.head)).toOption.get.head
          val matches = key == headTail.getKey

          if (matches) {
            dataRecords.append(headTail)
            dataLines.next()
            contextLines.next()
            vectorLines.next()

            readRecords()
          }
        }
      }

      val sentenceData = SentenceData(headDataRecord, headContextRecord, headVectorRecord, dataRecords)

      Some(sentenceData)
    }
  }

//  val urlSentenceIndexToTsvRecordMap: Map[(String, Int), LocalTsvRecord] = Using.resource(Sourcer.sourceFromFilename(inputDataFilename)) { source =>
//    val lines = source.getLines.drop(1)
//    val tsvReader = new TsvReader()
//
//    lines.map { line =>
//      val Array(url, sentenceIndexString, sentence, beliefString, sentimentScore, sentenceLocationsString, contextLocationsString, vectorString) = tsvReader.readln(line, 8)
//      val sentenceIndex = sentenceIndexString.toInt
//      val belief = beliefString == "True"
//      val sentimentScoreOpt = if (sentimentScore.isEmpty) None else Some(sentimentScore.toFloat)
//      val sentenceLocations = parseLocations(sentenceLocationsString)
//      val contextLocations = parseLocations(contextLocationsString)
//      val vector = normalize(parseVector(vectorString))
//
//      (url, sentenceIndex) -> LocalTsvRecord(sentenceIndex, sentence, belief, sentimentScoreOpt, sentenceLocations, contextLocations, vector)
//    }.toMap
//  }

  Using.resources(
    Elasticsearch.mkRestClient(url, credentialsFilename),
    Sourcer.sourceFromFilename(inputDataFilename),
    Sourcer.sourceFromFilename(inputContextFilename),
    Sourcer.sourceFromFilename(inputVectorFilename)
  ) { (restClient, dataSource, contextSource, vectorSource) =>
    val elasticsearchIndexClient = ElasticsearchIndexClient(restClient, indexName)
    val dataLines = dataSource.getLines.buffered
    val contextLines = contextSource.getLines.buffered
    val vectorLines = vectorSource.getLines.buffered
    val dataHeader = dataLines.next
    val contextHeader = contextLines.next
    val vectorHeader = vectorLines.next

    @tailrec
    def processLines(): Unit = {
      val sentenceDataOpt = getSentenceDataOpt(dataLines, contextLines, vectorLines, dataHeader, contextHeader, vectorHeader)

      if (sentenceDataOpt.nonEmpty) {
        val datasetRecord = sentenceDataOpt.get.toDatasetRecord

        // elasticsearchIndexClient.index(datasetRecord)
        processLines()
      }
    }

    processLines()
  }
}
