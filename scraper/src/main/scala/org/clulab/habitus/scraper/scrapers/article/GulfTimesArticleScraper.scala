package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.GulfTimesDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.net.URL

class GulfTimesArticleScraper extends PageArticleScraper(GulfTimesDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    // Extracting Title
    val title = (doc >?> element("h1.title-article")).map(_.text)


    val rawDateOpt = doc >?> element("time.publishing-date") map (_.text.trim)

    // If the text is always in the form: "Published on January 14, 2023 | 10:26 PM"
    val date = rawDateOpt.map { rawDate =>
      // Remove the fixed parts "Published on" and split by '|'
      val justDateTime = rawDate
        .replace("Published on ", "")
        .split("\\|")
        .map(_.trim)

      // justDateTime(0) -> "January 14, 2023"
      // justDateTime(1) -> "10:26 PM"

      // Combine them back or parse separately
      val datePart = LocalDateTime.parse(
        s"${justDateTime(0)} ${justDateTime(1)}",
        DateTimeFormatter.ofPattern("MMMM d, yyyy hh:mm a")
      )
      datePart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }


    // Extracting Author
    val author = (doc >?> element("span.article-byline")).map(_.text).orElse(Some("Unknown"))

    // Extracting Article Content
    val articleBodyOpt = doc >?> element("div.article-body")
    val text = articleBodyOpt.map { articleBody =>
      // Convert the inner HTML to paragraphs by splitting on <br> tags
      // and stripping any remaining HTML tags from each piece
      articleBody.innerHtml
        .split("(?i)<br\\s*/?>") // case-insensitive split on <br> or <br/>
        .map(_.replaceAll("<.*?>", "").trim) // remove any lingering HTML tags
        .filter(_.nonEmpty) // ignore empty lines
        .mkString("\n\n") // join with double newlines
    }.getOrElse("")

    // Extracting URL
    val urlString = (doc >?> element("meta[property='og:url']")).map(_.attr("content")).getOrElse(page.url.toString)
    val url = new URL(urlString)

    // Return structured article data
    ArticleScrape(url, title, date, author, text)
  }
}
