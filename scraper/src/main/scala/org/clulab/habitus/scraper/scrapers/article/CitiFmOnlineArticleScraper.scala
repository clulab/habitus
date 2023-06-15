package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.CitiFmOnlineDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape

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

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val dateline = (doc >> elementList("div.jeg_meta_date")).head.text.trim
    val paragraphs = (doc >> elementList("div.content-inner > p"))
        // Sometimes there are empty paragraphs after the byline so that it
        // isn't last.  Remove the empty ones preventatively.
        .filter { paragraph => paragraph.text.trim.nonEmpty }
    val bylineOpt = paragraphs.lastOption
        .flatMap { paragraph =>
          val text = paragraph.text.trim

          Byline.fromTextOption(text)
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
          !bylineOpt.exists(_.text == text)
        }
        .mkString("\n\n")

    ArticleScrape(page.url, Some(title), Some(dateline), bylineOpt.map(_.source), text)
  }
}

object CitiFmOnlineArticleScraper {
  val bylinePrefixes = Seq(
    "Source: ",
    "By: "
  )
  val contextlyRegex = "^\\[contextly_sidebar id=\u201D[^\u201D]+\u201D\\]".r
}
