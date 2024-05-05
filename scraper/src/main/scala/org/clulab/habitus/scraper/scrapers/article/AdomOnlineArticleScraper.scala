package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.AdomOnlineDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.{DefaultFormats, JArray, JObject}
import org.json4s.jackson.JsonMethods

class AdomOnlineArticleScraper extends PageArticleScraper(AdomOnlineDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def getTextFromParagraphs(elements: List[Element]): String = {
    val text = elements
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .filter(_ != "READ ALSO:")
      .filter(_ != "ALSO READ:")
      .mkString("\n\n")

    text
  }

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    def getTextOptFromDoc(description: String): Option[String] = {
      val paragraphs = doc >> elementList(description)
      val text = getTextFromParagraphs(paragraphs)

      if (text.isEmpty) None
      else Some(text)
    }

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
    val (datelineOpt, bylineOpt) =
        if (jObjectOpt.isDefined) {
          val jObject = jObjectOpt.get
          val graph = (jObject \ "@graph").extract[JArray]
          val newsArticle = graph.arr
              .find { jValue =>
                val string = (jValue \ "@type").extract[String]

                string == "Article" || string == "NewsArticle"
              }
              .get
          val personOpt = graph.arr
              .find { jValue =>
                val string = (jValue \ "@type").extract[String]

                string == "Person"
              }
          val dateline = (newsArticle \ "datePublished").extract[String]
          val bylineOpt: Option[String] = None
              .orElse((newsArticle \ "author" \ "name").extractOpt[String])
              .orElse(personOpt.map { person => (person \ "name").extract[String] })
              .orElse((newsArticle \ "author" \ "@id").extractOpt[String])

          (Some(dateline), bylineOpt)
        }
        else {
          val datelineOpt = (doc >> elementList("time.entry-date")).headOption.map(_.attr("datetime"))
          val bylineOpt = (doc >> elementList("div.td-post-source-via a")).headOption.map(_.text.trim)

          (datelineOpt, bylineOpt)
        }
    val text = None
        .orElse(getTextOptFromDoc("div.td-post-content > p"))
        .orElse(getTextOptFromDoc("div.td-post-content p"))
        .orElse(getTextOptFromDoc("div.td-post-content > div.article-text"))
        .orElse(getTextOptFromDoc("div.td-post-content > div.option-bar"))
        .getOrElse("")
    val text2 = if (text.isEmpty) {
      val elements = doc >> elementList(AdomOnlineArticleScraper.specialDescription)
      val text2 = getTextFromParagraphs(elements)

      if (text2.isEmpty)
        throw new RuntimeException("There was no text in the article!")
      text2
    }
    else text
    ArticleScrape(page.url, Some(title), datelineOpt, bylineOpt, text2)
  }
}

object AdomOnlineArticleScraper {
//  val specialClasses = Seq("article-body-item", "article__intro", "article__body", "post-single-content")
  val specialDescription = "" +
      "div.td-post-content > div, " +
      "div.td-post-content > div.article-body-item, " +
      "div.td-post-content > div.article__intro, " +
      "div.td-post-content > div.article__body, " +
      "div.td-post-content > div.post-single-content, " +
      "div.td-page.content > p"
}
