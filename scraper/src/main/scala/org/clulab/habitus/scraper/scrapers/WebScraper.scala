package org.clulab.habitus.scraper.scrapers

import org.clulab.habitus.scraper.corpora.SearchCorpus
import org.clulab.habitus.scraper.inquirers.CorpusInquirer
import org.clulab.utils.FileUtils

import scala.util.Using

class WebScraper(val corpus: SearchCorpus) {
  val corpusInquirer = new CorpusInquirer()

  def scrape(fileName: String): Unit = {
    Using.resource(FileUtils.printWriterFromFile(fileName)) { printWriter =>
      corpus.items.foreach { search =>
        val pageInquirer = corpusInquirer.getPageInquirer(search.page)
        val page = pageInquirer.inquire(search.inquiry)

        printWriter.println(s"${page.url.toString}\t${search.inquiry}")
      }
    }
  }
}
