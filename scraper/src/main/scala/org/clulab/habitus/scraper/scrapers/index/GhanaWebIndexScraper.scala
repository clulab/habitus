package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.scrapes.IndexScrape

class GhanaWebIndexScraper extends PageIndexScraper("ghanaweb.com") {
  val subDirName = "GhanaHomePage"

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val protocol = page.url.getProtocol
    val host = page.url.getHost
    val doc = browser.parseString(html)
    val hrefs = (doc >> elementList("div.search_result > a"))
        .map(_.attr("href"))
    val links = hrefs.map { href =>
      val link = s"$protocol://$host/$subDirName/$href"

      link
    }
    val scrape = IndexScrape(links)

    scrape
  }
}
