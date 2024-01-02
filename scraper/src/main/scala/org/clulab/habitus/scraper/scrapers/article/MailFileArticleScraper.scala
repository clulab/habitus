package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.MailDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.clulab.utils.StringUtils
import org.clulab.utils.FileUtils
import org.json4s.DefaultFormats

import java.io.File
import java.net.URL
import java.nio.file.Files
import scala.io.Source
import scala.util.Using

class MailFileArticleScraper extends PageArticleScraper(MailDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, mailLocationName: String): ArticleScrape = {
    Using.resource(Source.fromFile(mailLocationName)) { source =>
      val lines = source.getLines
      val headers = lines.takeWhile(_.nonEmpty).toList
      val text = lines.mkString
      val map = headers.map { header =>
        val key = header.takeWhile(_ != ':')
        val value = header.substring(key.length + 1).trim

        key -> value
      }.toMap
      val url = new URL(map("url"))
      val titleOpt = map.get("title")
      val datelineOpt = map.get("dateline")
      val bylineOpt = map.get("byline")

      ArticleScrape(url, titleOpt, datelineOpt, bylineOpt, text)
    }
  }

  override def scrapeTo(browser: Browser, page: Page, baseDirName: String): Unit = {
    val readFrom = StringUtils.beforeLast(s"$baseDirName/${page.url.getHost}${page.url.getPath}", '.')
    val scraped = scrape(browser, page, readFrom + ".mail")

    // The mail file could possibly be moved or copied to
    // live alongside the txt and json files.
    
    val dirName = cleaner.clean(scraped.url.getHost)
    val fileName = cleaner.clean(scraped.url.getFile)
    val writeTo = s"$baseDirName/$dirName"

    Files.createDirectories(new File(writeTo).toPath)

    val mailLocationName = s"$writeTo/$fileName.txt"
    val text = scraped.toText

    val jsonLocationName = s"$writeTo/$fileName.json"
    val json = scraped.toJson

    Using.resource(FileUtils.printWriterFromFile(mailLocationName)) { printWriter =>
      printWriter.println(text)
    }

    Using.resource(FileUtils.printWriterFromFile(jsonLocationName)) { printWriter =>
      printWriter.println(json)
    }
  }
}
