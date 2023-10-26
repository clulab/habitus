package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.UgStandardDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.clulab.utils.StringUtils

class UgStandardArticleScraper extends PageArticleScraper(UgStandardDomain) {

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val datelineOpt = (doc >> elementList("div.post-info-date time")).headOption.map(_.attr("datetime"))
    val bylineOpt = (doc >> elementList("div.post-info-name span.author-name a")).headOption.map(_.text.trim)
        .map { line => StringUtils.beforeFirst(line, '|', all = true).trim }
    val paragraphs = doc >> elementList("div#content-main > p")
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .mkString("\n\n")

    ArticleScrape(page.url, Some(title), datelineOpt, bylineOpt, text)
  }
}
