package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.MiningReviewDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JArray, JObject}

class MiningReviewArticleScraper extends PageArticleScraper(MiningReviewDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats
  val textElements = Array(
    "div.tdi_73 > div.tdb-block-inner > p",
    "div.tdi_73 > div.tdb-block-inner > h2",
    "div.tdi_73 > div.tdb-block-inner > h3",
    "div.tdi_73 > div.tdb-block-inner > h4",
    "div.tdi_73 > div.tdb-block-inner > ul > li",
    "div.tdi_73 > div.tdb-block-inner > ol > li"
  ).mkString(", ")

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
    val webPage = graph.arr
        .find { jValue =>
          (jValue \ "@type").extract[String] == "WebPage"
        }
        .get
    val dateline = (webPage \ "datePublished").extract[String]
    val personOpt = graph.arr
      .find { jValue =>
        (jValue \ "@type").extract[String] == "Person"
      }
    val bylineOpt = personOpt.map { person => (person \ "name").extract[String] }
    val paragraphs = doc >> elementList(textElements)
    val text = paragraphs
        .map { paragraph =>
          paragraph.text.trim
        }
        .filter(_.nonEmpty)
        .mkString("\n\n")

    if (text.contains('<'))
      throw new RuntimeException("HTML was found in text!")
    ArticleScrape(page.url, Some(title), Some(dateline), bylineOpt, text)
  }
}
