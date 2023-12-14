package org.clulab.habitus.apps.lucene

import ai.lum.common.FileUtils._
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, StoredField, StringField, TextField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import org.clulab.utils.StringUtils
import org.clulab.wm.eidoscommon.utils.{FileEditor, FileUtils}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.io.File
import java.net.URL
import java.nio.file.Paths
import scala.util.Using

object LuceneIndexerApp extends App {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  val luceneDirname = "../lucene"
//  val baseDirname = "../corpora/uganda-local/karamoja"
  val baseDirname = "/home/kwa/data/Projects/habitus-project/corpora/uganda-pdfs"
  val country = "Uganda" // "Ghana"
  val googlePrefix = "https://customsearch.googleapis.com/"

  case class JsonFileRecord(jsonFile: File, filename: String, term: String)

  def newIndexWriter(dir: String): IndexWriter = {
    val analyzer = new StandardAnalyzer()
    val config = new IndexWriterConfig(analyzer)
    val index = FSDirectory.open(Paths.get(dir))

    new IndexWriter(index, config)
  }

  def newDocument(
    url: URL,
    titleOpt: Option[String],
    datelineOpt: Option[String],
    bylineOpt: Option[String],
    text: String,
    country: String,
    terms: Seq[String]
  ): Document = {
    val document = new Document()

    // We can search the text, country, terms, and maybe date fields.
    // These are the non-StoredFields with Field.Store.YES.
    document.add(new StoredField("url", url.toString))
    document.add(new StoredField("title", titleOpt.getOrElse("")))
    document.add(new StoredField("dateline", datelineOpt.getOrElse("")))
    document.add(new StoredField("byline", bylineOpt.getOrElse("")))
    document.add(new TextField("text", text, Field.Store.YES))
    document.add(new StringField("country", country, Field.Store.YES))
    document.add(new StringField("terms", terms.mkString("\t"), Field.Store.YES))
    document
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

  def jsonFileToJsonld(jsonFile: File): File =
      FileEditor(jsonFile).setExt("jsonld").get

  val jsonldFiles = new File(baseDirname).listFilesByWildcard("*.jsonld", recursive = true).toSet
  val allJsonFiles = new File(baseDirname).listFilesByWildcard("*.json", recursive = true)
      // TODO remove take
      // .take(1000)
  val jsonFiles = allJsonFiles
      .filter { jsonFile => jsonldFiles(jsonFileToJsonld(jsonFile)) } // Make sure it has an a corresponding jsonld file.
  val jsonFileRecords = jsonFiles.map { jsonFile =>
    val filename = jsonFile.getName
    val term = jsonFileToTerm(jsonFile)

    JsonFileRecord(jsonFile, filename, term)
  }
  val jsonFilenameGroups = jsonFileRecords.groupBy(_.filename)
  val jsonFilenameToTerms = jsonFilenameGroups.map { case (_, jsonFileRecords) =>
    val terms = jsonFileRecords.map(_.term).toSeq
    val jsonFile = jsonFileRecords.head.jsonFile

    jsonFile -> terms
  }

  Using.resource(newIndexWriter(luceneDirname)) { indexWriter =>
    jsonFilenameToTerms.foreach { case (jsonFile, terms) =>
      val json = FileUtils.getTextFromFile(jsonFile)
      val jValue = JsonMethods.parse(json)
      val url = {
        val rawUrl = (jValue \ "url").extract[String]
        val url =
            if (rawUrl.startsWith(googlePrefix))
              rawUrl.drop(googlePrefix.length)
            else rawUrl

        new URL(url)
      }
      val titleOpt = (jValue \ "title").extractOpt[String]
      val datelineOpt = (jValue \ "dateline").extractOpt[String]
      val bylineOpt = (jValue \ "byline").extractOpt[String]
      val text = (jValue \ "text").extract[String]
      val document = newDocument(
        url, titleOpt, datelineOpt, bylineOpt, text, country, terms
      )

      indexWriter.addDocument(document)
    }

    indexWriter.commit()
  }
}
