package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JArray, JObject, JNothing}

class TheChronicleArticleScraper extends PageArticleScraper("thechronicle.com.gh") {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val jObject = (doc >> elementList("script"))
        .find { element =>
          element.hasAttr("type") && element.attr("type") == "application/ld+json"
        }
        .map { element =>
          val json = element.innerHtml
          val jObject = JsonMethods.parse(json).asInstanceOf[JObject]

          jObject
        }
        .get
    val graph = (jObject \ "@graph").extract[JArray]
    val dateline = (graph(0) \ "datePublished").extract[String]
    val bylineOpt = graph.arr
        .find { jValue =>
          (jValue \ "@type").extract[String] == "Person"
        }
        .map { jValue => (jValue \ "name").extract[String] }
    val paragraphs = doc >> elementList("div.td-post-content > p")
    val text = paragraphs
        .map { paragraph =>
          paragraph.text.trim
        }
        .filter(_.nonEmpty)
        .mkString("\n\n")

    ArticleScrape(page.url, Some(title), Some(dateline), bylineOpt, text)
  }
}
