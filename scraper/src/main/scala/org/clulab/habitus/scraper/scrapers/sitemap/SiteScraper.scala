package org.clulab.habitus.scraper.scrapers.sitemap

import org.clulab.habitus.scraper.{Cleaner, Page}
import org.clulab.utils.FileUtils

import scala.xml.{Elem, XML, Source => XMLSource}

trait SiteScraper {

  def isSitemap(elem: Elem): Boolean = elem.label == "urlset"

  def isSitemapIndex(elem: Elem): Boolean = elem.label == "sitemapindex"

  def toXml(string: String): Elem = {
    val xml = XML.load(XMLSource.fromString(string))

    xml
  }

  def readPage(page: Page, baseDirName: String, cleaner: Cleaner): String = {
    val domain = page.url.getHost.split('.')/*.takeRight(2)*/.mkString(".") // Shorten to one .
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"
    val pageFileName = cleaner.clean(page.url.getFile)
    val pageLocationName = s"$subDirName/$pageFileName"
    val text = FileUtils.getTextFromFile(pageLocationName)

    text
  }
}
