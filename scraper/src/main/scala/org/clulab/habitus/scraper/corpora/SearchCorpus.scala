package org.clulab.habitus.scraper.corpora

import org.clulab.habitus.scraper.Search

class SearchCorpus(val fileName: String) extends Corpus[Search] {
  override val items: Seq[Search] = {
    val lines = getLines(fileName)
    val searches = lines.map { line =>
      val Array(url, term) = line.split('\t')

      Search(url, term)
    }
    searches
  }
}

object SearchCorpus {

  def apply(fileName: String): SearchCorpus = {
    new SearchCorpus(fileName)
  }
}
