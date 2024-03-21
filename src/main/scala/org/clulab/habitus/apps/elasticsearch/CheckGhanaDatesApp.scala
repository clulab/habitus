package org.clulab.habitus.apps.elasticsearch

import ai.lum.common.FileUtils._
import org.clulab.habitus.apps.utils.JsonRecord
import org.clulab.wm.eidos.serialization.jsonld.JLDDeserializer
import org.clulab.wm.eidoscommon.utils._
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.{BasicFileAttributes, FileTime}
import scala.util.Using

object CheckGhanaDatesApp extends App with Logging {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  val baseDirectory = "/home/kwa/data/Corpora/habitus-project/corpora/multimix"
  val outputFileName = "../filedates.tsv"
  val deserializer = new JLDDeserializer()

  def jsonFileToJsonld(jsonFile: File): File =
      FileEditor(jsonFile).setExt("jsonld").get

  def jsonFileToHtml(jsonFile: File): File =
    FileEditor(jsonFile).setExt("html").get

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

  case class FileRecord(url: String, file: File, lastModifiedTimeOpt: Option[FileTime])

  val fileRecords: Seq[FileRecord] = {
    val jsonFiles = new File(baseDirectory).listFilesByWildcard("*.json", recursive = true).toVector
    val urls = jsonFiles.map(jsonFileToRecord(_).url)
    val fileTimeOpts = jsonFiles.map { jsonFile =>
      // val jsonldFile = jsonFileToJsonld(jsonFile)
      val htmlFile = jsonFileToJsonld(jsonFile)
      val basicFileAttributesOpt =
          try {
            Some(Files.readAttributes(htmlFile.toPath, classOf[BasicFileAttributes]))
          }
          catch {
            case _: Throwable => None
          }
      val lastModifiedTimeOpt = basicFileAttributesOpt.map(_.lastModifiedTime)

      lastModifiedTimeOpt
    }

    (urls, jsonFiles, fileTimeOpts).zipped.toVector.map { tuple => FileRecord(tuple._1, tuple._2, tuple._3) }
  }
  val fileRecordMap = fileRecords.groupBy(_.url)

  Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
    val tsvWriter = new TsvWriter(printWriter)

    tsvWriter.println("url", "fileName", "modifiedTime")
    fileRecordMap.map { case (_, fileRecords) =>
      fileRecords.foreach { fileRecord =>
        val date = fileRecord.lastModifiedTimeOpt.map { lastModifiedTime =>
          StringUtils.beforeFirst(lastModifiedTime.toString, 'T')
        }.getOrElse("")

        tsvWriter.println(fileRecord.url, fileRecord.file.getCanonicalFile.toString, date)
      }
    }
  }
}
