package org.clulab.habitus.scraper.scrapers.sitemap

import scala.xml.{Elem, XML, Source => XMLSource}

trait SiteScraper {

  def isSitemap(elem: Elem): Boolean = elem.label == "urlset"

  def isSitemapIndex(elem: Elem): Boolean = elem.label == "sitemapindex"

  def toXml(string: String): Elem = {
    val xml = XML.load(XMLSource.fromString(string))

    xml
  }

}
