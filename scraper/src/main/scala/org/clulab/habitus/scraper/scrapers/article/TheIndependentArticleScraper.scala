package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.TheIndependentDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.json4s.DefaultFormats

class TheIndependentArticleScraper extends PageArticleScraper(TheIndependentDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val dateLineOpt = (doc >> elementList("span.tie-date")).headOption.map(_.text.trim)
    val paragraphs = doc >> elementList("div.entry > p")
    val byLineOpt = (doc >> elementList("span.post-meta-author")).headOption.map(_.text.trim)
        .orElse {
          val bylines = paragraphs.flatMap { paragraph =>
            val strongs = (paragraph >> elementList("p > strong"))
                .map(_.text.trim)
                .filter(_.startsWith("By "))

            strongs
          }
          val byLineOpt = bylines.headOption

          byLineOpt
        }
        .orElse((doc >> elementList("span.post-cats > a")).headOption.map(_.text.trim))
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .mkString("\n\n")

    ArticleScrape(page.url, Some(title), dateLineOpt, byLineOpt, text)
  }
}
