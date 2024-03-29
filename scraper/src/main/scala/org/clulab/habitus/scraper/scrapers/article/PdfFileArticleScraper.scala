package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.PdfDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape

import org.clulab.utils.FileUtils
import org.json4s.DefaultFormats

import java.io.File
import scala.util.Using

class PdfFileArticleScraper extends PageArticleScraper(PdfDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, pdfLocationName: String): ArticleScrape = {
    val rawText = GoogleArticleScraper.pdf2txt.read(new File(pdfLocationName), None)
    val text = GoogleArticleScraper.pdf2txt.process(rawText, GoogleArticleScraper.loops)
    val pdfMetadata = GoogleArticleScraper.readPdfMetadata(pdfLocationName)

    ArticleScrape(page.url, pdfMetadata.titleOpt, pdfMetadata.datelineOpt, pdfMetadata.bylineOpt, text)
  }

  def readPdf(page: Page, baseDirName: String): (String, String, String) = {
    // val dirName = cleaner.clean(domain.domain)
    val subDirName = s"$baseDirName" // $dirName"
    val file = page.url.getFile.drop(1) // cleaner.clean(page.url.getFile)

    val pdfFileName = file // + ".pdf"
    val pdfLocationName = s"$subDirName/$pdfFileName"

    (subDirName, file, pdfLocationName)
  }

  override def scrapeTo(browser: Browser, page: Page, baseDirName: String): Unit = {
    val (subDirName, file, pdfLocationName) = readPdf(page, baseDirName)
    val scraped = scrape(browser, page, pdfLocationName)

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
