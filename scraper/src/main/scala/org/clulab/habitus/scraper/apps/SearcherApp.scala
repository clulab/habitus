package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.clulab.habitus.scraper.Corpus
import org.clulab.habitus.scraper.scrapers.GhanaWebSearchScraper
import org.clulab.utils.FileUtils

object SearcherApp extends App {
  val baseDirName = args.lift(0).getOrElse("../corpora/searcher")
  val corpus = Seq(
//    "Search in News Archive on ghanaweb-2.html",
//    "Search in News Archive on ghanaweb-3.html",
//    "Search in News Archive on ghanaweb-4.html",
//    "Search in News Archive on ghanaweb-5.html",
//    "Search in News Archive on ghanaweb-6.html",
//    "Search in News Archive on ghanaweb-7.html",
//    "Search in News Archive on ghanaweb-8.html",
//    "Search in News Archive on ghanaweb-9.html",
//    "Search in News Archive on ghanaweb-10.html",

    "Search in News Archive on ghanaweb-11.html",
    "Search in News Archive on ghanaweb-12.html",
    "Search in News Archive on ghanaweb-13.html",
    "Search in News Archive on ghanaweb-14.html",
    "Search in News Archive on ghanaweb-15.html",
    "Search in News Archive on ghanaweb-16.html",
    "Search in News Archive on ghanaweb-17.html",
    "Search in News Archive on ghanaweb-18.html",
    "Search in News Archive on ghanaweb-19.html",
    "Search in News Archive on ghanaweb-20.html"
  )
  val browser: Browser = JsoupBrowser()
  val scraper: GhanaWebSearchScraper = new GhanaWebSearchScraper("https://ghanaweb.com/GhanaHomePage")

  corpus.foreach { fileName =>
    val location = s"$baseDirName/$fileName"
    val html = FileUtils.getTextFromFile(location)

    scraper.scrape(browser, html)
  }
}
