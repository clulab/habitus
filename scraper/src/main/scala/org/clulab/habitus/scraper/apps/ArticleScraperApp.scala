package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.browsers.HabitusBrowser
import org.clulab.habitus.scraper.corpora.PageCorpus
import org.clulab.habitus.scraper.scrapers.article.CorpusArticleScraper

object ArticleScraperApp extends App {
  val term = "uganda farming"
  val corpusFileName = args.lift(0).getOrElse(s"./scraper/corpora/uganda/$term/articlecorpus.txt")
  val baseDirName = args.lift(1).getOrElse(s"../corpora/uganda/$term/articles")
  val corpus = PageCorpus(corpusFileName)
  val scraper = new CorpusArticleScraper(corpus)
  val browser: Browser = new HabitusBrowser()

  scraper.scrape(browser, baseDirName)
}
