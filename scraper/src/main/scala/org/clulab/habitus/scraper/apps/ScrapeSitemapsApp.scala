package org.clulab.habitus.scraper.apps

import org.clulab.utils.{FileUtils, StringUtils}
import sttp.client4.quick._
import sttp.model.{Header, HeaderNames, MediaType, Uri}

import java.io.PrintWriter
import java.net.URL
import scala.io.Source
import scala.util.Using
import scala.xml.{Elem, XML, Source => XMLSource}

object ScrapeSitemapsApp extends App {
  val urlString = args.lift(0).getOrElse("https://miningreview.com")
  val outFileName = args.lift(1).getOrElse("../sitemaps/" + StringUtils.afterLast(urlString, '/') + ".pages")

  val robotsString = urlString + "/robots.txt"
  val sitemapIndexString = urlString + "/sitemap_index.xml"
  val sitemapString = urlString + "/sitemap.xml"

  def getSitemapIndexesFromRobots(robotsString: String): Seq[String] = {
    val header = "Sitemap:"
    val response = quickRequest
        .get(Uri.unsafeParse(robotsString))
        // .header(Header(HeaderNames.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0"))
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

  def run(printWriter: PrintWriter): Unit = {

    def println(string: String): Unit = {
      printWriter.println(string)
      System.out.println(string)
    }

    println("Sitemap indexes:\n")
    val sitemapIndexes = {
      val sitemapIndexes = {
        val sitemapIndexes = getSitemapIndexesFromRobots(robotsString)

        sitemapIndexes.map { sitemapIndex =>
          val url = new URL(sitemapIndex)
          val path = url.getPath

          if (path == "/.xml") StringUtils.beforeLast(sitemapIndex, '/') + "/sitemap.xml"
          else path
        }
      }
      sitemapIndexes.foreach(println)
      val distinctSitemapIndexes = (sitemapIndexes :+ sitemapIndexString).distinct

      distinctSitemapIndexes
    }
    println("\n")
    println("Sitemaps:\n")
    val sitemaps = {
      val sitemaps = sitemapIndexes.flatMap { sitemapIndex =>
        val sitemaps = getSitemapsFromSitemapIndex(sitemapIndex)
        sitemaps.foreach(println)
        sitemaps
      }
      val distinctSitemaps = (sitemaps :+ sitemapString).distinct

      distinctSitemaps
    }
    println("\n")
    println("Sites:\n")
    val sites = {
      val sites = sitemaps.flatMap { sitemap =>
        val sites = getSitesFromSitemap(sitemap)
        sites.foreach(println)
        sites
      }
      val distinctSites = sites.distinct

      distinctSites
    }
    println("\n")
    println("Finished!")
  }

  Using.resource(FileUtils.printWriterFromFile(outFileName)) { printWriter =>
    run(printWriter)
  }
}
