package org.clulab.habitus.scraper.scrapers.search

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.{Cleaner, Page, Search}
import org.clulab.habitus.scraper.domains.GoogleDomain
import org.clulab.habitus.scraper.inquirers.{GoogleInquirer, PageInquirer}
import org.clulab.habitus.scraper.scrapes.SearchScrape
import org.clulab.utils.FileUtils
import org.json4s.{DefaultFormats, JObject}
import org.json4s.jackson.JsonMethods

import java.io.PrintWriter

class GoogleSearchScraper extends PageSearchScraper(GoogleDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, json: String): SearchScrape = {
    val jObject = JsonMethods.parse(json).asInstanceOf[JObject]
    val count = (jObject \ "queries" \ "request").extract[Array[JObject]].array.map { jObject =>
      (jObject \ "totalResults").extract[String].toInt
    }.head
    val scrape = SearchScrape(count)

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

  override def scrapeTo(browser: Browser, page: Page, inquirer: PageInquirer, search: Search, baseDirName: String, printWriter: PrintWriter): Unit = {
    val templatePage = page
    val (_, _, json) = readJson(page, baseDirName)
    val scraped = scrape(browser, page, json)
    val pageCount = math.min(math.ceil(scraped.count / GoogleInquirer.PER_PAGE).toInt, GoogleSearchScraper.LIMIT)

    1.to(pageCount).foreach { index =>
      val page = inquirer.inquire(search.inquiry, Some(index), Some(templatePage))

      printWriter.println(page.url)
    }
  }
}

object GoogleSearchScraper {
  val LIMIT = 10
}
