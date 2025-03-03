package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.ABCDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

import java.net.URL

class ABCArticleScraper extends PageArticleScraper(ABCDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    // Extracting Title
    val title = (doc >?> element("h1.Typography_base__sj2RP"))
      .map(_.text)

    // Extracting Date
    val date = (doc >?> element("time.Typography_base__sj2RP"))
      .map(_.text)


    // Extracting Article Content
    val paragraphs = doc >> elementList("div.DetailLayout_body__1rSCR p")
    val text = paragraphs.map(_.text.trim).filter(_.nonEmpty).mkString("\n\n")

    // Extracting URL
    val urlString = (doc >?> element("meta[property='og:url']")).map(_.attr("content")).getOrElse(page.url.toString)
    val url = new URL(urlString)

    // Return structured article data
    ArticleScrape(url, title, date, None, text)
  }
}
