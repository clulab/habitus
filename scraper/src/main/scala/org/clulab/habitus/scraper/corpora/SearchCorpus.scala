package org.clulab.habitus.scraper.corpora

import org.clulab.habitus.scraper.Search

class SearchCorpus(override val items: Seq[Search]) extends Corpus[Search] {
}

object SearchCorpus {

  def apply(fileName: String): SearchCorpus = {
    val items: Seq[Search] = {
      val lines = Corpus.getLines(fileName)
      val searches = lines.map { line =>
        val Array(url, term) = line.split('\t')

        Search(url, term)
      }
      searches
    }

    new SearchCorpus(items)
  }
}
