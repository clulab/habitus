package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.clulab.habitus.scraper.Corpus
import org.clulab.habitus.scraper.scrapers.index.GhanaWebIndexScraper
import org.clulab.utils.FileUtils

object IndexScraperApp extends App {
  val corpusFileName = args.lift(0).getOrElse("./scraper/indexcorpus.txt")
  val articleIndexFileName = args.lift(1).getOrElse("../articlecorpus.txt")
  val corpus = Corpus(corpusFileName)
  val scraper: GhanaWebIndexScraper = new GhanaWebIndexScraper("https://ghanaweb.com/GhanaHomePage")
  val browser: Browser = JsoupBrowser()

  corpus.foreach { fileName =>
    val location = s"$baseDirName/$fileName"
    val html = FileUtils.getTextFromFile(location)

    scraper.scrape(browser, html)
  }
}
