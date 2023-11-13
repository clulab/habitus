package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.KfmDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape

class KfmArticleScraper extends PageArticleScraper(KfmDomain) {

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val datelineOpt = (doc >> elementList("div.entry-header div.jeg_meta_date > a")).headOption.map(_.text.trim)
    val bylineOpt = (doc >> elementList("div.entry-header div.jeg_meta_author > a")).headOption.map(_.text.trim)
    val paragraphs = doc >> elementList("div.content-inner > p")
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .mkString("\n\n")

    ArticleScrape(page.url, Some(title), datelineOpt, bylineOpt, text)
  }
}
