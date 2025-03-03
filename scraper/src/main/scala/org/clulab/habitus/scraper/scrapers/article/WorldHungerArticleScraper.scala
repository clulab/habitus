package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.WorldHungerDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

import java.net.URL

class WorldHungerArticleScraper extends PageArticleScraper(WorldHungerDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    // Extract Title
    val title = (doc >?> element("h1 a")).map(_.text)


    // Extract Year from Date
    val date = (doc >?> element("h2.post-data.item")).map(_.text)
      .flatMap(_.split(" ").find(_.forall(_.isDigit))) // Extracts numeric year like "2025"

    // Extract Author (Byline)
    val author = (doc >?> element("span.article-byline")).map(_.text)
      .orElse(Some("Unknown"))

    val paragraphs = doc >> elementList("div.rgt_sec p")
    val text = paragraphs.map(_.text.trim).filter(_.nonEmpty).mkString("\n\n")
    // Extract URL
    val urlString = (doc >?> element("meta[property='og:url']")).map(_.attr("content")).getOrElse(page.url.toString)
    val url = new URL(urlString)

    // Return structured article data
    ArticleScrape(url, title, date, author, text)
  }
}
