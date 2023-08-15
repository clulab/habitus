package org.clulab.habitus.scraper.scrapes

class SearchScrape(val count: Int)

object SearchScrape {

  def apply(count: Int): SearchScrape = new SearchScrape(math.min(100, count))
}