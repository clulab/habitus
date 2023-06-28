package org.clulab.habitus.scraper.apps

import org.clulab.habitus.scraper.corpora.SearchCorpus
import org.clulab.habitus.scraper.scrapers.WebScraper

/**
  * Take the list of URLs in corpusFileName, find the downloaded pages, scrape them,
  * and put the links found into articleFileName so that they can be downloaded and
  * later scraped themselves.
  */

object WebScraperApp extends App {
  val corpusFileName = args.lift(0).getOrElse("./scraper/webcorpus-illegal-mining.txt")
  val searchFileName = args.lift(1).getOrElse("./scraper/searchcorpus-illegal-mining.txt")
  val corpus = SearchCorpus(corpusFileName)
  val scraper = new WebScraper(corpus)

  scraper.scrape(searchFileName)
}
