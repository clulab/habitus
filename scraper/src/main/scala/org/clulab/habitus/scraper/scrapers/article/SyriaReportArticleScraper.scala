package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.SyriaReportDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

class SyriaReportArticleScraper extends PageArticleScraper(SyriaReportDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    // Extract Title
    val title = (doc >?> element("meta[property='og:title']")).map(_.attr("content"))
      .orElse(doc >?> element("title").map(_.text))

    // Extract Publication Date
    val rawDate = doc >?> element("time.date-container.minor-meta.updated") map (_.text)

    val date = rawDate.flatMap { dateStr =>
      Try {
        val parsed = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        // 3. Convert the parsed date to ISO 8601, e.g. "2020-10-14"
        parsed.format(DateTimeFormatter.ISO_LOCAL_DATE)
      }.toOption
    }
    // Extract Author
    val author = (doc >?> element("meta[name='author']")).map(_.attr("content")).orElse(Some("Syria Report"))

    // Extract Article Content
    val paragraphs = doc >> elementList(".entry-content p, .post-content p, .article-body p")
    val text = paragraphs.map(_.text.trim).filter(_.nonEmpty).mkString("\n\n")

    // Extract URL
    val urlString = (doc >?> element("meta[property='og:url']")).map(_.attr("content")).getOrElse(page.url.toString)
    val url = new URL(urlString) // Ensure it's a valid URL type

    // Return structured article data
    ArticleScrape(url, title, date, author, text)
  }
}
