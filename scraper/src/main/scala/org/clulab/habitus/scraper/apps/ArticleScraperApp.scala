package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.browsers.HabitusBrowser
import org.clulab.habitus.scraper.corpora.PageCorpus
import org.clulab.habitus.scraper.scrapers.article.CorpusArticleScraper

object ArticleScraperApp extends App {
  val term = "interview"
  val corpusFileName = args.lift(0).getOrElse(s"./scraper/corpora/interviews/uganda/articlecorpus.txt")
  val baseDirName = args.lift(1).getOrElse("../corpora/interviews/uganda/articles")
  val corpus = PageCorpus(corpusFileName)
  val scraper = new CorpusArticleScraper(corpus)
  val browser: Browser = new HabitusBrowser()

  scraper.scrape(browser, baseDirName)
}
