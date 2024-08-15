package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.DocxDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.clulab.utils.FileUtils
import org.json4s.{DefaultFormats, JString}

import java.io.{ByteArrayInputStream, File}
import java.nio.file.{Files => JFiles}
import scala.jdk.CollectionConverters._
import scala.util.Using

class DocxFileArticleScraper extends PageArticleScraper(DocxDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats
  val tika = new Tika()

  def scrape(browser: Browser, page: Page, docxLocationName: String): ArticleScrape = {
    val bytes = JFiles.readAllBytes(new File(docxLocationName).toPath)
    val mimeType = tika.detect(new ByteArrayInputStream(bytes))

    require(mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document")

    def isDate(line: String): Boolean = {
      line.forall { char => char.isDigit || char == '/' || char == ' '}
    }

    val metadata = new Metadata()
    val text = tika.parseToString(new ByteArrayInputStream(bytes), metadata)
    val byline = metadata.get("meta:last-author")
    val dateline = metadata.get("dcterms:modified")
    val title = new File(docxLocationName).getName.takeWhile(_ != '.')
    val lines = text.lines().iterator().asScala.toVector
    val lines1 = lines.dropWhile(_.isBlank)
    assert(lines1.head.contains("Transcript"))
    val lines2 = lines1.drop(1)
    val lines3 =
      if (isDate(lines2.head)) lines2.drop(1)
      else {
        val lines = lines2.drop(1)

        if (isDate(lines.head)) lines.drop(1)
        else {
          assert(false)
          lines
        }
      }
    val headless = lines3.dropWhile(_.isBlank)
    val tailless = headless.reverse.dropWhile { line =>
      line.isBlank || line.forall(_.isDigit) || line.contains("Page")
    }.reverse
    val patchedText = tailless.mkString("\n")

    ArticleScrape(page.url, Some(title), Some(dateline), Some(byline), patchedText)
  }

  def readDocx(page: Page, baseDirName: String): (String, String, String) = {
    // val dirName = cleaner.clean(domain.domain)
    val subDirName = s"$baseDirName" // $dirName"
    val file = page.url.getFile.drop(1) // cleaner.clean(page.url.getFile)

    val docxFileName = file // + ".docx"
    val docxLocationName = s"$subDirName/$docxFileName"

    (subDirName, file, docxLocationName)
  }

  override def scrapeTo(browser: Browser, page: Page, baseDirName: String): Unit = {
    val (subDirName, file, docxLocationName) = readDocx(page, baseDirName)
    val scraped = scrape(browser, page, docxLocationName)

    val txtFileName = file + ".txt"
    val txtLocationName = s"$subDirName/$txtFileName"
    val text = scraped.toText

    val jsonFileName = file + ".json"
    val jsonLocationName = s"$subDirName/$jsonFileName"
    val json = scraped.toJson

    Using.resource(FileUtils.printWriterFromFile(txtLocationName)) { printWriter =>
      printWriter.println(text)
    }

    Using.resource(FileUtils.printWriterFromFile(jsonLocationName)) { printWriter =>
      printWriter.println(json)
    }
  }
}
