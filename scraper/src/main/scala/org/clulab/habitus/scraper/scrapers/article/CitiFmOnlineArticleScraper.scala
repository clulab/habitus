package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.CitiFmOnlineDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.{DefaultFormats, JArray, JObject}
import org.json4s.jackson.JsonMethods

case class Byline(text: String, prefix: String) {
  val source = text.substring(prefix.length)
}

object Byline {
  val prefixes = Seq(
    "Source: ",
    "By: "
  )

  def fromTextOption(text: String): Option[Byline] = {
    val prefixOpt = prefixes.find { sourcePrefix =>
      text.startsWith(sourcePrefix)
    }
    val bylineOpt = prefixOpt.map(Byline(text, _))

    bylineOpt
  }
}

class CitiFmOnlineArticleScraper extends PageArticleScraper(CitiFmOnlineDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)

    def getParagraphsOpt(description: String): Option[List[Element]] = {
      val paragraphs = (doc >> elementList(description))
        // Sometimes there are empty paragraphs after the byline so that it
        // isn't last.  Remove the empty ones preventatively.
        .filter { paragraph => paragraph.text.trim.nonEmpty }

      if (paragraphs.isEmpty) None
      else Some(paragraphs)
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
    val paragraphs = None
        .orElse(getParagraphsOpt("div.content-inner > p"))
        // Sometimes they put something in between the div and p.
        .orElse(getParagraphsOpt("div.content-inner p"))
        .getOrElse(List.empty)
    val (datelineOpt, bylineOpt) =
        if (jObjectOpt.isDefined) {
          val jObject = jObjectOpt.get
          val graph = (jObject \ "@graph").extract[JArray]
          val newsArticle = graph.arr
              .find { jValue =>
                val string = (jValue \ "@type").extract[String]

                string == "Article" || string == "NewsArticle" || string == "WebPage"
              }
              .getOrElse {
                val matchOpt = """<script type="application/ld\+json" class="yoast-schema-graph">(.*?)</script>""".r.findFirstMatchIn(html)
                val jsonOpt = matchOpt.map(_.group(1))
                val jObjectOpt = jsonOpt.map { json => JsonMethods.parse(json).asInstanceOf[JObject] }

                println(jsonOpt)
                println("What is wrong here?")
                throw new RuntimeException("Something is wrong!")
              }
          val personOpt = graph.arr
              .find { jValue =>
                val string = (jValue \ "@type").extract[String]

                string == "Person"
              }
          val dateline = (newsArticle \ "datePublished").extract[String]
          val bylineOpt: Option[String] = None
              .orElse((newsArticle \ "author" \ "name").extractOpt[String])
              .orElse(personOpt.flatMap { person => (person \ "name").extractOpt[String] })
              .orElse((newsArticle \ "author" \ "@id").extractOpt[String])
              .flatMap { byLine => if (byLine.nonEmpty) Some(byLine) else None }

          (Some(dateline), bylineOpt)
        }
        else {
          val dateline = (doc >> elementList("div.jeg_meta_date")).head.text.trim
          val bylineOpt = paragraphs.lastOption
              .flatMap { paragraph =>
                val text = paragraph.text.trim

                Byline.fromTextOption(text).map(_.source)
              }
          (Some(dateline), bylineOpt)
        }
    val text = paragraphs
        .map { paragraph =>
          val text = paragraph.text.trim
          val regexMatchOpt = CitiFmOnlineArticleScraper.contextlyRegex.findFirstMatchIn(text)

          regexMatchOpt
              .map { regexMatch =>
                text.substring(regexMatch.end).trim
              }
              .getOrElse(text)
        }
        .filter { text =>
          text.nonEmpty && // After the regex match it might be empty.
          text != "\u2013" &&
          !bylineOpt.exists(_ == text)
        }
        .mkString("\n\n")

    ArticleScrape(page.url, Some(title), datelineOpt, bylineOpt, text)
  }
}

object CitiFmOnlineArticleScraper {
  val bylinePrefixes = Seq(
    "Source: ",
    "By: "
  )
  val contextlyRegex = "^\\[contextly_sidebar id=\u201D[^\u201D]+\u201D\\]".r
}
