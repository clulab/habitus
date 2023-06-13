package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JArray, JObject}

class HappyGhanaArticleScraper extends PageArticleScraper("www.happyghana.com") {
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
    val newsArticle = graph.arr
        .find { jValue =>
          (jValue \ "@type").extract[String] == "NewsArticle"
        }
        .get
    val dateline = (newsArticle \ "datePublished").extract[String]
    val personOpt = graph.arr
      .find { jValue =>
        (jValue \ "@type") match {
          case jArray: JArray => jArray(0).extract[String] == "Person"
          case _ => false
        }
      }
    val bylineOpt = personOpt.map { person => (person \ "name").extract[String] }
    val paragraphs = doc >> elementList("div.content-inner > p")
    val text = paragraphs
        .map { paragraph =>
          paragraph.text.trim
        }
        .filter(_.nonEmpty)
        .filterNot(_.startsWith("READ MORE: "))
        .mkString("\n\n")

    if (text.contains('<'))
      throw new RuntimeException("HTML was found in text!")
    ArticleScrape(page.url, Some(title), Some(dateline), bylineOpt, text)
  }
}
