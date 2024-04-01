package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.InterviewDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.clulab.utils.FileUtils
import org.clulab.wm.eidoscommon.utils.FileEditor
import org.json4s.DefaultFormats

import java.io.File
import scala.util.Using

case class InterviewMetadata(titleOpt: Option[String], datelineOpt: Option[String], bylineOpt: Option[String])

class InterviewFileArticleScraper extends PageArticleScraper(InterviewDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, iviewLocationName: String): ArticleScrape = {
    val file = page.url.getFile.drop(1)
    val text = FileUtils.getTextFromFile(iviewLocationName)
    val metadata = InterviewFileArticleScraper.metadataMap(file)

    ArticleScrape(page.url, metadata.titleOpt, metadata.datelineOpt, metadata.bylineOpt, text)
  }

  def readIview(page: Page, baseDirName: String): (String, String, String) = {
    // See PdfFileArticleScraper for example of how these were derived
    // from the non-file versions.
    val subDirName = s"$baseDirName"
    val file = page.url.getFile.drop(1)
    val iviewLocationName = s"$baseDirName/$file"

    (subDirName, file, iviewLocationName)
  }

  override def scrapeTo(browser: Browser, page: Page, baseDirName: String): Unit = {
    val (subDirName, file, iviewLocationName) = readIview(page, baseDirName)
    val scraped = scrape(browser, page, iviewLocationName)
    val text = scraped.toText
    val textLocationName = FileEditor(new File(iviewLocationName)).setExt("txt").get
    val jsonLocationName = FileEditor(new File(iviewLocationName)).setExt("json").get
    val json = scraped.toJson

    Using.resource(FileUtils.printWriterFromFile(textLocationName)) { printWriter =>
      printWriter.println(text)
    }

    Using.resource(FileUtils.printWriterFromFile(jsonLocationName)) { printWriter =>
      printWriter.println(json)
    }
  }
}

object InterviewFileArticleScraper {
  val metadataMap: Map[String, InterviewMetadata] = Map(
    "TranscriptUGA-E0006_original.iview" -> InterviewMetadata(
      Some("Interview with NBS TV Karamoja, original version"),
      Some("2024-01-26"),
      Some("UGA-E0006")
    ),
    "TranscriptUGA-E0006_case.iview" -> InterviewMetadata(
      Some("Interview with NBS TV Karamoja, distinguished by case"),
      Some("2024-01-26"),
      Some("UGA-E0006")
    ),
    "TranscriptUGA-E0006_respondent.iview" -> InterviewMetadata(
      Some("Interview with NBS TV Karamoja, respondent only"),
      Some("2024-01-26"),
      Some("UGA-E0006")
    )
  ).withDefaultValue(InterviewMetadata(None, None, None))
}
