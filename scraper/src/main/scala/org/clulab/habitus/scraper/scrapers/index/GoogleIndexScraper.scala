package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.GoogleDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape
import org.clulab.utils.FileUtils
import org.json4s.{DefaultFormats, JObject}
import org.json4s.jackson.JsonMethods

import java.io.PrintWriter

class GoogleIndexScraper extends PageIndexScraper(GoogleDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, json: String): IndexScrape = {
    val jObject = JsonMethods.parse(json).asInstanceOf[JObject]
    val links = (jObject \ "items").extract[Array[JObject]].array.map { jObject =>
      (jObject \ "link").extract[String]
    }
    val prefix = s"https://${domain.domain}/"
    val googleLinks = links.map { link => prefix + link}
    val scrape = IndexScrape(googleLinks)

    scrape
  }

  def readJson(page: Page, baseDirName: String): (String, String, String) = {
    val dirName = cleaner.clean(domain.domain)
    val subDirName = s"$baseDirName/$dirName"
    val file = cleaner.clean(page.url.getFile)

    val jsonFileName = file + ".json"
    val jsonLocationName = s"$subDirName/$jsonFileName"
    val json = FileUtils.getTextFromFile(jsonLocationName)

    (subDirName, file, json)
  }

  override def scrapeTo(browser: Browser, page: Page, baseDirName: String, printWriter: PrintWriter): Unit = {
    val (_, _, json) = readJson(page, baseDirName)
    val scraped = scrape(browser, page, json)

    scraped.links.foreach { link =>
      printWriter.println(link)
    }
  }
}
