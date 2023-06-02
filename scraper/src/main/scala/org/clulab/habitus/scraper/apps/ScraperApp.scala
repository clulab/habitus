package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.clulab.habitus.scraper.Corpus

object ScraperApp extends App {
  val baseDirName = args.lift(0).getOrElse("../corpora/scraper")
  val corpus = Corpus()
  val browser: Browser = JsoupBrowser()

  corpus.scrape(browser, baseDirName)
}
