package org.clulab.habitus.scraper.corpora

import org.clulab.habitus.scraper.Page

class PageCorpus(override val items: Seq[Page]) extends Corpus[Page] {
}

object PageCorpus {

  def apply(fileName: String): PageCorpus = {
    val items: Seq[Page] = {
      val lines = Corpus
          .getLines(fileName)
          .filterNot(_.startsWith("#"))
      val pages = lines.map(Page(_))

      pages
    }

    new PageCorpus(items)
  }
}
