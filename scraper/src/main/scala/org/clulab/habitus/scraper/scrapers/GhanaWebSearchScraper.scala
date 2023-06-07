package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.SearchScrape

import java.net.URL

class GhanaWebSearchScraper(home: String) {

  def scrape(browser: Browser, html: String): Unit = {
    val doc = browser.parseString(html)
    val links = (doc >> elementList("div.search_result > a"))
        .map(_.attr("href"))
    val searchScrapes = links.map { link =>
      val url = s"$home/$link"

      SearchScrape(new URL(url))
    }

    searchScrapes.foreach { searchScrape =>
      println(searchScrape.toPage)
    }
  }
}
