package org.clulab.habitus.scraper.scrapes

import java.net.URL

case class IndexScrape(url: URL) {

  def toText: String = {
    url.toString
  }

  def toPage: String = {
    s"""Page("${url.toString}"),"""
  }
}
