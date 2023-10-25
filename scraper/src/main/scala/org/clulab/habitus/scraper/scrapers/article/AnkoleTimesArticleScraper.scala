package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.AnkoleTimesDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape

class AnkoleTimesArticleScraper extends PageArticleScraper(AnkoleTimesDomain) {

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val datelineOpt = (doc >> elementList("div.td-ss-main-content span.td-post-date time.entry-date")).headOption.map(_.attr("datetime"))
    val bylineOpt = (doc >> elementList("div.td-ss-main-content div.td-post-author-name a")).headOption.map(_.text.trim)
    val paragraphs = doc >> elementList("div.td-post-content > p, div.td-post-content div.markdown > p")
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .mkString("\n\n")

    ArticleScrape(page.url, Some(title), datelineOpt, bylineOpt, text)
  }
}
