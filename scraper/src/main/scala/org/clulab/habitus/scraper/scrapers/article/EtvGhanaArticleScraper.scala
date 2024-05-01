package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.EtvGhanaDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JArray, JObject}

class EtvGhanaArticleScraper extends PageArticleScraper(EtvGhanaDomain) {
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
          val typ = (jValue \ "@type").extract[String]
          typ == "NewsArticle" | typ == "WebPage"
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
    val bylineOpt = personOpt.flatMap { person => (person \ "name").extractOpt[String] }
    val paragraphs = {
      val outerParagraphs = doc >> elementList("div.content-inner > p")

      if (outerParagraphs.nonEmpty) outerParagraphs
      else doc >> elementList("div.content-inner p")
    }
    val text = paragraphs
        .map { paragraph =>
          paragraph.text.trim
        }
        .map { text =>
          text
            .replaceAll("<iframe .*?</iframe>", "")
            .replace("<div style=”position:relative;height:0;padding-bottom:58.33%”></div>", "")
            .replaceAll("<blockquote .*?</blockquote> <script .*?</script>", "")
        }
        .filter(_.nonEmpty)
        .mkString("\n\n")

    if (text.contains('<') && !(text.contains("<[email protected]>") || text.contains("<-Advertisement->")))
      throw new RuntimeException("HTML was found in text!")
    ArticleScrape(page.url, Some(title), Some(dateline), bylineOpt, text)
  }
}
