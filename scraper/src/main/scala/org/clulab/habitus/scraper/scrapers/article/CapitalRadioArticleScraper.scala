package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.CapitalRadioDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.clulab.utils.StringUtils

class CapitalRadioArticleScraper extends PageArticleScraper(CapitalRadioDomain) {

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val datelineOpt = (doc >> elementList("div.article-meta div.article-published")).headOption.map(_.text.trim)
    val bylineOpt = (doc >> elementList("div.article-meta span.author-name span")).headOption.map(_.text.trim)
        .map { line => StringUtils.beforeFirst(line, '|', all = true).trim }
    val paragraphs = doc >> elementList("div.article-widgets div.text > p")
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .mkString("\n\n")

    ArticleScrape(page.url, Some(title), datelineOpt, bylineOpt, text)
  }
}
