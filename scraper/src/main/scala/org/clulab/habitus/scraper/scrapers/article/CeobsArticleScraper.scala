package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.CeobsDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

import java.net.URL

class CeobsArticleScraper extends PageArticleScraper(CeobsDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    // Extracting Title
    val title = (doc >?> element("meta[property='og:title']")).map(_.attr("content"))
      .orElse(doc >?> element("title").map(_.text))

    // Extracting Publication Date
    val date = (doc >?> element("time.entry-date.updated")).map(_.attr("datetime"))
      .orElse(doc >?> element("time.entry-date.updated").map(_.text))

    // Extracting Author
    val author = (doc >?> element("meta[name='author']")).map(_.attr("content")).orElse(Some("Environmental Conflicts"))

    // Extracting Article Content
    // Extracting Article Content from <span> inside <p>
    val paragraphs = doc >> elementList("p span, p")

    // Removing hyperlinks while keeping surrounding text
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.replaceAll("\\s+", " ").trim // Normalize spaces
      }
      .filter(_.nonEmpty)
      .mkString("\n\n") // Ensure proper paragraph separation

    // Extracting URL
    val urlString = (doc >?> element("meta[property='og:url']")).map(_.attr("content")).getOrElse(page.url.toString)
    val url = new URL(urlString)

    // Return structured article data
    ArticleScrape(url, title, date, author, text)
  }
}
