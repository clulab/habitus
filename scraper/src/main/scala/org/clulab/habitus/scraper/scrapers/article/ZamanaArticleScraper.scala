package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.ZamanaDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

import java.net.URL

class ZamanaArticleScraper extends PageArticleScraper(ZamanaDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    val title = (doc >?> element("h1.entry-title")).map(_.text)
      .orElse(doc >?> element("title").map(_.text))

    // Extract Publication Date
    val dateRaw = (doc >?> element("h2.post-data.item")).map(_.text)
    val date = dateRaw.flatMap(_.split(" ").find(_.matches("\\d{4}")))

    // Extract Author (Fallback to "Syrian Times" if not found)
    val author = (doc >?> element("meta[name='author']")).map(_.attr("content")).orElse(Some("Zaman Alwsl"))

    // Extracting Article Content
    val paragraphs = doc >> elementList(".entry-content p, p.selectionShareable")
    val text = paragraphs.map(_.text.trim).filter(_.nonEmpty).mkString("\n\n")


    // Extract URL
    val urlString = (doc >?> element("meta[property='og:url']")).map(_.attr("content")).getOrElse(page.url.toString)
    val url = new URL(urlString) // Ensure it's a valid URL type

    // Return structured article data
    ArticleScrape(url, title, date, author, text)
  }
}
