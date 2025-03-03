package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.MercyCorpsDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

import java.net.URL

class MercyCorpsArticleScraper extends PageArticleScraper(MercyCorpsDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    // Extracting Title
    val title = (doc >?> element("h1.c-page-title__title span")).map(_.text)

    // Extracting Publication Date
    val date = (doc >?> element("div.c-story-header-full__date")).map(_.text)

    // Extracting Article Content
    val paragraphs = doc >> elementList("div[data-history-node-id] p span")
    val text = paragraphs.map(_.text.trim).filter(_.nonEmpty).mkString("\n\n")

    // Extracting URL
    val urlString = (doc >?> element("meta[property='og:url']")).map(_.attr("content")).getOrElse(page.url.toString)
    val url = new URL(urlString)

    // Return structured article data
    ArticleScrape(url, title, date, None, text)
  }
}
