package org.clulab.habitus.scraper

class Search(val page: Page, val inquiry: String)

object Search {

  def apply(urlName: String, inquiry: String): Search = new Search(Page(urlName), inquiry)
}
