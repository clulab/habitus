package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.browsers.HabitusBrowser
import org.clulab.habitus.scraper.corpora.PageCorpus
import org.clulab.habitus.scraper.scrapers.index.CorpusIndexScraper

/**
  * Take the list of URLs in corpusFileName, find the downloaded pages, scrape them,
  * and put the links found into articleFileName so that they can be downloaded and
  * later scraped themselves.
  */

object IndexScraperApp extends App {
  val term = "pastoralist"
  val corpusFileName = args.lift(0).getOrElse(s"./scraper/corpora/uganda/$term/indexcorpus.txt")
  val articleFileName = args.lift(1).getOrElse(s"./scraper/corpora/uganda/$term/articlecorpus.txt")
  val baseDirName = args.lift(1).getOrElse(s"../corpora/uganda/$term/indexes")

  val corpus = PageCorpus(corpusFileName)
  val scraper = new CorpusIndexScraper(corpus)
  val browser: Browser = new HabitusBrowser()

  scraper.scrape(browser, baseDirName, articleFileName)
}
