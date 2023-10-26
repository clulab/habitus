package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.NbsDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape

class NbsArticleScraper extends PageArticleScraper(NbsDomain) {

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val datelineOpt = (doc >> elementList("div.main div.post-date")).headOption.map(_.text.trim)
    val bylineOpt = (doc >> elementList("div.main div.post-author")).headOption.map(_.text.trim)
    val paragraphs = doc >> elementList("div.main div.article-content > p, div.main div.article-content div.markdown > p")
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .filterNot { text => text.head.isUpper && text.last == ':' && !text.contains('.') }
      .filterNot { text => text.startsWith("http") && text.last == '/' && !text.contains(' ') }
      .filterNot { text => text.head.isUpper && text.last == '/' && !text.contains(". ") }
      .mkString("\n\n")

    ArticleScrape(page.url, Some(title), datelineOpt, bylineOpt, text)
  }
}
