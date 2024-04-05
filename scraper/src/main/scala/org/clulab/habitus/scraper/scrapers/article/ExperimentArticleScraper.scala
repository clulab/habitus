package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.ExperimentDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.clulab.utils.FileUtils
import org.clulab.wm.eidoscommon.utils.FileEditor
import org.json4s.DefaultFormats

import java.io.File
import scala.util.Using

class ExperimentArticleScraper extends PageArticleScraper(ExperimentDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, textLocationName: String): ArticleScrape = {
    val text = FileUtils.getTextFromFile(textLocationName)

    ArticleScrape(page.url, None, None, None, text)
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
    val (_, _, textLocationName) = readText(page, baseDirName)
    val scraped = scrape(browser, page, textLocationName)
    val jsonLocationName = FileEditor(new File(textLocationName)).setExt("json").get
    val json = scraped.toJson

    Using.resource(FileUtils.printWriterFromFile(jsonLocationName)) { printWriter =>
      printWriter.println(json)
    }
  }
}

object ExperimentArticleScraper
