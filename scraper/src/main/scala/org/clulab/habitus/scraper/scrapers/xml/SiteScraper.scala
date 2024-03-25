package org.clulab.habitus.scraper.scrapers.xml

import org.clulab.habitus.scraper.domains.Domain
import org.clulab.habitus.scraper.scrapers.search.PageSearchScraper

import scala.xml.{Elem, XML, Source => XMLSource}

abstract class SiteScraper(domain: Domain) extends PageSearchScraper(domain) {

  def isSitemap(elem: Elem): Boolean = elem.label == "urlset"

  def isSitemapIndex(elem: Elem): Boolean = elem.label == "sitemapindex"

  def toXml(string: String): Elem = {
    val xml = XML.load(XMLSource.fromString(string))

    xml
  }

}
