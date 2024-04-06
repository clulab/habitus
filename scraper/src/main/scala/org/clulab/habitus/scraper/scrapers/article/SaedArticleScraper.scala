package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.SaedDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.clulab.utils.FileUtils
import org.clulab.wm.eidoscommon.utils.FileEditor
import org.json4s.DefaultFormats

import java.io.File
import scala.util.Using

class SaedArticleScraper extends PageArticleScraper(SaedDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats
  val looseRegex = ".*_(\\d\\d)_(\\d\\d)_(\\d\\d\\d\\d)\\..*".r // Bulletin_22_12_2020.en.txt
  val tightRegex = "^(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)_.*".r // 20201015_Bulletin-SAED_no13.fr.en.txt
  val map = Map(
    "2020810_Bulletin_SAED_no32.fr.en.txt" -> "2020-08-10",
    "Bulletin_SAED13.fr.en.txt" -> "2008-10-14",
    "Bulletin_SAED18.fr.en.txt" -> "2009-01-13",
    "Bulletin_SAED082009.fr.en.txt" -> "2009-06-30",
    "Bulletin_SAED_09_2008.fr.en.txt" -> "2008-09-23",
    "Bulletin-SAEDoct2008.fr.en.txt" -> "2008-10-30"
  )

  def scrape(browser: Browser, page: Page, textLocationName: String): ArticleScrape = {
    val file = page.url.getFile.drop(1)
    val text = FileUtils.getTextFromFile(textLocationName)
    val title = file
    val byline = "SAED"
    val dateline = file match {
      case looseRegex(day, month, year) => s"$year-$month-$day"
      case tightRegex(year, month, day) => s"$year-$month-$day"
      case _ => map(file)
    }
    val pdfLocationName = FileEditor(new File(textLocationName)).setExt("pdf").get.getAbsolutePath
    val pdfMetadata = GoogleArticleScraper.readPdfMetadata(pdfLocationName)

    // ArticleScrape(page.url, Some(title), Some(dateline), Some(byline), text)
    ArticleScrape(page.url, pdfMetadata.titleOpt, pdfMetadata.datelineOpt, pdfMetadata.bylineOpt, text)
  }

  def readText(page: Page, baseDirName: String): (String, String, String) = {
    // See PdfFileArticleScraper for example of how these were derived
    // from the non-file versions.
    val subDirName = s"$baseDirName"
    val file = page.url.getFile.drop(1)
    val textLocationName = s"$baseDirName/$file"

    (subDirName, file, textLocationName)
  }

  override def scrapeTo(browser: Browser, page: Page, baseDirName: String): Unit = {
    val (subDirName, file, textLocationName) = readText(page, baseDirName)
    val scraped = scrape(browser, page, textLocationName)
    val jsonLocationName = FileEditor(new File(textLocationName)).setExt("json").get
    val json = scraped.toJson

    Using.resource(FileUtils.printWriterFromFile(jsonLocationName)) { printWriter =>
      printWriter.println(json)
    }
  }
}

object SaedArticleScraper
