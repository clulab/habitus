package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.{Page, Scrape}
import org.json4s.{DefaultFormats, JObject}
import org.json4s.jackson.JsonMethods

class GhanaWebScraper extends Scraper("ghanaweb.com") {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): Scrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val jObjectOpt = (doc >> elementList("script"))
        .find { element =>
          element.hasAttr("type") && element.attr("type") == "application/ld+json"
        }
        .map { element =>
          val json = element.innerHtml
          val jObject = JsonMethods.parse(json).asInstanceOf[JObject]

          jObject
        }
    val dateline = jObjectOpt.map { jObject =>
      ((jObject \ "@graph")(0) \ "datePublished").extract[String]
    }.get
    val bylineOpt = jObjectOpt.map { jObject =>
      ((jObject \ "@graph")(0) \ "publisher" \ "name").extract[String]
    }
    val paragraphs = doc >> elementList("p#article-123")
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

    Scrape(page.url, Some(title), Some(dateline), bylineOpt, text)
  }
}
