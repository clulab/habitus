package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.DocxDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.clulab.utils.FileUtils
import org.json4s.DefaultFormats

import java.io.{ByteArrayInputStream, File}
import java.nio.file.{Files => JFiles}
import scala.util.Using

class DocxFileArticleScraper extends PageArticleScraper(DocxDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats
  val tika = new Tika()

  def scrape(browser: Browser, page: Page, docxLocationName: String): ArticleScrape = {
    val bytes = JFiles.readAllBytes(new File(docxLocationName).toPath)
    val mimeType = tika.detect(new ByteArrayInputStream(bytes))

    require(mimeType == "application/x-tika-ooxml")

    val metadata = new Metadata()
    val text = tika.parseToString(new ByteArrayInputStream(bytes), metadata)

    println(metadata.names())

    ArticleScrape(page.url, None, None, None, text)
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
