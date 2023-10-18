package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.TheObserverDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

class TheObserverArticleScraper extends PageArticleScraper(TheObserverDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val dateLineOpt = (doc >> elementList("ul > li > time")).headOption.map(_.attr("datetime").trim)
    val byLineOpt = (doc >> elementList("ul > li.createdby > span"))
        .filter(_.attr("itemprop") == "name").headOption.map(_.text.trim)
    // Use any p or a div without a class.
    val paragraphs = (doc >> elementList("div.itemBody p, div.itemBody div"))
      .filter { element =>
        element.tagName == "p" || (
          element.tagName == "div" && !element.hasAttr("class") && element.children.forall(_.tagName != "div")
        )
      }
    val text = paragraphs
      .flatMap { paragraph =>
        val html = paragraph.innerHtml
        val paragraphs = html.split("<br><br>").map(_.trim).toSeq
        val docs = paragraphs.map { paragraph => browser.parseString(s"<html><body>$paragraph</body></html>") }
        val texts = docs.map { doc => (doc >> elementList("body")).head.text.trim }

        texts
      }
      .filter(_.nonEmpty)
      .mkString("\n\n")

    ArticleScrape(page.url, Some(title), dateLineOpt, byLineOpt, text)
  }
}
