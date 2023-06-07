package org.clulab.habitus.scraper

import java.net.URL

case class SearchScrape(url: URL) {

  def toText: String = {
    url.toString
  }

  def toPage: String = {
    s"""Page("${url.toString}"),"""
  }
}
