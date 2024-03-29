package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.ThreeNewsDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JArray, JObject}

class ThreeNewsArticleScraper extends PageArticleScraper(ThreeNewsDomain) {
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
          (jValue \ "@type").extract[String] == "Article"
        }
        .get
    val dateline = (newsArticle \ "datePublished").extract[String]
    val byline = (newsArticle \ "author" \ "name").extract[String]

    val paragraphs = doc >> elementList("div.td-main-content-wrap div.td-main-content div.td-post-content p")
    val text = paragraphs
        .map { paragraph =>
          paragraph.text.trim
        }
        .filter(_.nonEmpty)
        .mkString("\n\n")
    val cleanText =
        if (text.contains('<')) {
          val html = s"<html><body><div>$text</div></body></html>"
          val doc = browser.parseString(html)
          val cleanText = (doc >> elementList("body > div")).head.text.replace("]]>", "").trim

          if (cleanText.contains('<'))
            throw new RuntimeException("HTML was found in text!")
          cleanText
        }
        else text

    ArticleScrape(page.url, Some(title), Some(dateline), Some(byline), cleanText)
  }
}
