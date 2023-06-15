package org.clulab.habitus.scraper.corpora

import org.clulab.habitus.scraper.Page

class PageCorpus(val fileName: String) extends Corpus[Page] {
  override val items: Seq[Page] = {
    val lines = getLines(fileName)
    val pages = lines.map(Page(_))

    pages
  }
}

object PageCorpus {

  def apply(fileName: String): PageCorpus = {
    new PageCorpus(fileName)
  }
}
