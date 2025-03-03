package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.SyriaTimesDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

import java.net.URL

class SyriaTimesArticleScraper extends PageArticleScraper(SyriaTimesDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    // Extract Title
    val title = (doc >?> element("meta[property='og:title']")).map(_.attr("content"))
      .orElse(doc >?> element("title").map(_.text))

    // Extract Publication Date
    // Extract Publication Date
    val date = (doc >?> element("time.post-published.updated")).map(_.attr("datetime"))
      .orElse(doc >?> element("time.post-published.updated b").map(_.text))

    // Extract Author (Fallback to "Syrian Times" if not found)
    val author = (doc >?> element("meta[name='author']")).map(_.attr("content")).orElse(Some("Syrian Times"))

    // Extract Article Content
    val paragraphs = doc >> elementList(".entry-content p")
    val text = paragraphs.map(_.text.trim).filter(_.nonEmpty).mkString("\n\n")

    // Extract URL
    val urlString = (doc >?> element("meta[property='og:url']")).map(_.attr("content")).getOrElse(page.url.toString)
    val url = new URL(urlString) // Ensure it's a valid URL type

    // Return structured article data
    ArticleScrape(url, title, date, author, text)
  }
}
