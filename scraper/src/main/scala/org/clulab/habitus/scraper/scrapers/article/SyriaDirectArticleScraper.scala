package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.SyriaDirectDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

class SyriaDirectArticleScraper extends PageArticleScraper(SyriaDirectDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    // Extracting Title
    val title = (doc >?> element("meta[property='og:title']")).map(_.attr("content"))
      .orElse(doc >?> element("title").map(_.text))

    // Extracting Publication Date
    val date = (doc >?> element("meta[property='article:published_time']")).map(_.attr("content"))

    // Extracting Author
    val author = (doc >?> element("meta[name='author']")).map(_.attr("content")).orElse(Some("Syria Direct"))

    // Extracting Article Content
    val paragraphs = doc >> elementList("article p, .entry-content p, .post-content p")
    val text = paragraphs.map(_.text.trim).filter(_.nonEmpty).mkString("\n\n")

    // Return structured article data
    ArticleScrape(page.url, title, date, author, text)
  }
}
