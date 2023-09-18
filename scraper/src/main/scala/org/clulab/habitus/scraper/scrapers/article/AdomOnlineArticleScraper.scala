package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.AdomOnlineDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape

class AdomOnlineArticleScraper extends PageArticleScraper(AdomOnlineDomain) {

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val datelineOpt = (doc >> elementList("time.entry-date")).headOption.map(_.attr("datetime"))
    val bylineOpt = (doc >> elementList("div.td-post-source-via a")).headOption.map(_.text.trim)
    val paragraphs = doc >> elementList("div.td-post-content > p")
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .filter(_ != "READ ALSO:")
      .mkString("\n\n")

    ArticleScrape(page.url, Some(title), datelineOpt, bylineOpt, text)
  }
}
