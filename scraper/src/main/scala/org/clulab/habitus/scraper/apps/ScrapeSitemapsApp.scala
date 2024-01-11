package org.clulab.habitus.scraper.apps

import sttp.client4.quick._
import sttp.model.{MediaType, Uri}

import scala.io.Source
import scala.xml.{Elem, XML, Source => XMLSource}

object ScrapeSitemapsApp extends App {
  val urlString = args.lift(0).getOrElse("https://3news.com")
  val robotsString = urlString + "/robots.txt"
  val sitemapIndexString = urlString + "/sitemap_index.xml"
  val sitemapString = urlString + "/sitemap.xml"
  val sitemapIndexes = {
    val sitemapIndexes = getSitemapIndexesFromRobots(robotsString)
    sitemapIndexes.foreach(println)
    val distinctSitemapIndexes = (sitemapIndexes :+ sitemapIndexString).distinct

    distinctSitemapIndexes
  }
  val sitemaps = {
    val sitemaps = sitemapIndexes.flatMap { sitemapIndex =>
      val sitemaps = getSitemapsFromSitemapIndex(sitemapIndex)
      sitemaps.foreach(println)
      sitemaps
    }
    val distinctSitemaps = (sitemaps :+ sitemapString).distinct

    distinctSitemaps
  }
  val sites = {
    val sites = sitemaps.flatMap { sitemap =>
      val sites = getSitesFromSitemap(sitemap)
      sites.foreach(println)
      sites
    }
    val distinctSites = sites.distinct

    distinctSites
  }

  println()
  println("=" * 50)
  println()
  sites.foreach(println)

  def getSitemapIndexesFromRobots(robotsString: String): Seq[String] = {
    val header = "Sitemap:"
    val response = quickRequest
        .get(Uri.unsafeParse(robotsString))
        .contentType(MediaType.TextPlain)
        .send()

    if (!response.isSuccess)
      Seq.empty
    else {
      val body = response.body
      val sitemapIndexes = Source.fromString(body).getLines().flatMap { line =>
        if (line.startsWith(header))
          Some(line.drop(header.length).trim)
        else
          None
      }.toVector

      sitemapIndexes
    }
  }

  def isSitemap(elem: Elem): Boolean = elem.label == "urlset"

  def isSitemapIndex(elem: Elem): Boolean = elem.label == "sitemapindex"

  def getSitemapsFromSitemapIndex(sitemapIndexString: String): Seq[String] = {

    def getSitemapsFromElem(elem: Elem): Seq[String] = {
      val sitemaps = (elem \\ "sitemap" \\ "loc").map { elem =>
        elem.text
      }

      sitemaps
    }

    val response = quickRequest
        .get(Uri.unsafeParse(sitemapIndexString))
        .contentType(MediaType.ApplicationXml)
        .send()

    if (!response.isSuccess)
      Seq.empty
    else {
      val body = response.body
      val xml = XML.load(XMLSource.fromString(body))

      if (isSitemap(xml))
        Seq(sitemapIndexString)
      else if (!isSitemapIndex(xml))
        Seq.empty
      else
        getSitemapsFromElem(xml)
    }
  }

  def getSitesFromSitemap(sitemapString: String): Seq[String] = {

    def getSitesFromElem(elem: Elem): Seq[String] = {
      val sites = (elem \\ "url" \\ "loc").map { elem =>
        elem.text
      }

      sites
    }

    val response = quickRequest
        .get(Uri.unsafeParse(sitemapString))
        .contentType(MediaType.ApplicationXml)
        .send()

    if (!response.isSuccess)
      Seq.empty
    else {
      val body = response.body
      val xml = XML.load(XMLSource.fromString(body))

      if (!isSitemap(xml))
        Seq.empty
      else
        getSitesFromElem(xml)
    }
  }
}
