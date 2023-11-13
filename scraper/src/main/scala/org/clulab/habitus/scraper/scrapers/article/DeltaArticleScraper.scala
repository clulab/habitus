package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.DeltaDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JArray, JObject, JString}

import scala.util.Try

class DeltaArticleScraper extends PageArticleScraper(DeltaDomain) {

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    implicit val formats: DefaultFormats.type = DefaultFormats

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
          Try((jValue \ "@type").extract[String] == "WebPage").getOrElse(false)
        }
        .get
    val dateline = (newsArticle \ "datePublished").extract[String]
//    val datelineOpt = (doc >> elementList("span.elementor-post-info__item--type-date")).headOption.map(_.text.trim)
    val personOpt = graph.arr
        .find { jValue =>
          (jValue \ "@type") match {
            case jArray: JArray => jArray(0).extract[String] == "Person"
            case jString: JString => jString.extract[String] == "Person"
            case _ => false
          }
        }
    val bylineOpt = personOpt.map { person => (person \ "name").extract[String] }

//    val bylineOpt = (doc >> elementList("span.elementor-post-info__item--type-author")).headOption.map(_.text.trim)
    val paragraphs = doc >> elementList("div.elementor-widget-theme-post-content > div > p")
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .mkString("\n\n")

    ArticleScrape(page.url, Some(title), Some(dateline), bylineOpt, text)
  }
}
