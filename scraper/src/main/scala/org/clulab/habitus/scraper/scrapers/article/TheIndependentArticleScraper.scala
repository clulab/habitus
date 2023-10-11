package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.ThreeNewsDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JArray, JObject}

class TheIndependentArticleScraper extends PageArticleScraper(TheIndependentDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val dateLineOpt = (doc >> elementList("span.tie-date")).headOption.map(_.text.trim)
    val byLineOpt = (doc >> elementList("span.post-cats > a")).headOption.map(_.text.trim)
    val paragraphs = doc >> elementList("div.entry > p")
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .mkString("\n\n")
//    val bylines = paragraphs.flatMap { paragraph =>
//        val strongs = paragraph >> elementList("p > strong")
//        val bylines = strongs
//            .map { strong =>
//              strong.text.trim
//            }
//            .filter { text =>
//              text.startsWith("By ")
//            }
//
//        bylines
//    }
//    val bylineOpt = bylines.headOption

    ArticleScrape(page.url, Some(title), dateLineOpt, byLineOpt, text)
  }
}
