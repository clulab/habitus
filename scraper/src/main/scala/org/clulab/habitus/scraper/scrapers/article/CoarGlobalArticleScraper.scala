package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.CoarGlobalDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

import java.net.URL

class CoarGlobalArticleScraper extends PageArticleScraper(CoarGlobalDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    // Extract Title
    val title = (doc >?> element("meta[property='og:title']")).map(_.attr("content"))
      .orElse(doc >?> element("title").map(_.text))

    // Extract Publication Date
    val date = (doc >?> element("meta[property='article:published_time']")).map(_.attr("content"))

    // Extract Author
    val author = (doc >?> element("meta[name='author']")).map(_.attr("content")).orElse(Some("COAR Global"))

    // Extract Article Content (Handles different formats)
    val paragraphs = doc >> elementList("p[style='text-align: justify;'], p[style='text-align: justify; text-indent: 70px;'], span[style='font-weight: 400;']")
    val text = paragraphs.map(_.text.trim).filter(_.nonEmpty).mkString("\n\n")

    // Extract URL
    val urlString = (doc >?> element("meta[property='og:url']")).map(_.attr("content")).getOrElse(page.url.toString)
    val url = new URL(urlString) // Ensure it's a valid URL type

    // Return structured article data
    ArticleScrape(url, title, date, author, text)
  }
}
