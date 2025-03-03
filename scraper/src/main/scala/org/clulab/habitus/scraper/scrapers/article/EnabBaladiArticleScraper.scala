package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.EnabBaladiDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

class EnabBaladiArticleScraper extends PageArticleScraper(EnabBaladiDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    // Extracting Title
    val title = (doc >?> element("meta[property='og:title']")).map(_.attr("content"))
      .orElse(doc >?> element("title").map(_.text))

    // Extracting Publication Date
    val date = (doc >?> element("meta[property='article:published_time']")).map(_.attr("content"))

    // Extracting Author
    val author = (doc >?> element("meta[name='author']")).map(_.attr("content")).orElse(Some("Enab Baladi"))

    // Extracting Article Content (Handles <p><span> tags)
    val paragraphs = doc >> elementList("p span, p")
    val text = paragraphs.map(_.text.trim).filter(_.nonEmpty).mkString("\n\n")

    // Extracting URL
    val url = (doc >?> element("meta[property='og:url']")).map(_.attr("content")).getOrElse(page.url.toString)

    // Return structured article data
    ArticleScrape(new java.net.URL(url), title, date, author, text)
  }
}
