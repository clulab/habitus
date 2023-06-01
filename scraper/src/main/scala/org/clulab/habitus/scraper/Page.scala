package org.clulab.habitus.scraper

import java.net.URL

class Page(val url: URL) {

  // way to get directory
  // way to get scraper
 // URL
  // filename
  // scraper
  // each scraper will have an assigned directory
  // download (from URL), plainload from file, scrape (text) and save to file
  // html file vs text file

  // Group these into collections by scraper
  // Then call the scraper on all of them
}

object Page {

  def apply(urlName: String): Page = new Page(new URL(urlName))
}
