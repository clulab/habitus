package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.GhanaWebDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JObject}

import scala.util.Try

class GhanaWebArticleScraper extends PageArticleScraper(GhanaWebDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val jObjectOpt = (doc >> elementList("script"))
        .find { element =>
          element.hasAttr("type") && element.attr("type") == "application/ld+json"
        }
        .map { element =>
          val json = element.innerHtml.replace("\t", "  ")

          val jObjectTry1 = Try { JsonMethods.parse(json).asInstanceOf[JObject] }
          val jObjectTry2 = jObjectTry1.orElse(Try { JsonMethods.parse(json.filter(c => c >= 0x20)).asInstanceOf[JObject] })
          val jObjectTry3 = jObjectTry2.orElse(Try { JsonMethods.parse(json.replace("\\", "\\\\")).asInstanceOf[JObject] })
          val jObjectTry4 = jObjectTry3.orElse { Try {
            val json2 = json
              .replace("\\\",", "\",") // They have escaped the trailing quotes in the json
              .replace("\\/", "/") // And forward slashes as well.

            JsonMethods.parse(json2)
          }}

          jObjectTry4.get
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

    if (text.isEmpty)
      println("Why?")
    ArticleScrape(page.url, Some(title), Some(dateline), bylineOpt, text)
  }
}
