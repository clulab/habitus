package org.clulab.habitus.scraper.apps

import org.clulab.utils.{Sourcer, StringUtils}
import org.clulab.habitus.scraper.utils.TsvReader

import java.net.{URL, URLEncoder}
import java.nio.charset.StandardCharsets
import scala.util.Using

object VerifySitemapsApp extends App {

  def extractFile(line: String): String = {
    val url = new URL(line.dropRight(1))

    url.getFile()
  }

  val urlString = args.lift(0).getOrElse("https://3news.com")
  val sitemapFileName = args.lift(1).getOrElse("../corpora/pages/" + StringUtils.afterLast(urlString, '/') + ".pages")
  val corpusFileName = "../corpora/ghana/dataset55k.tsv"
  val tsvReader = new TsvReader()
  val urlStart = "https://3news.com/"

  val sitemapUrls = Using.resource(Sourcer.sourceFromFilename(sitemapFileName)) { source =>
    source.getLines.map(extractFile).toSet
  }

  Using.resource(Sourcer.sourceFromFilename(corpusFileName)) { source =>
    val lines = source.getLines.drop(1)
    val urls = lines.map { line =>
      val head = tsvReader.readln(line).head
      val encoded = head.flatMap { letter =>
        if (letter.toInt > 127)
          URLEncoder.encode(letter.toString, StandardCharsets.UTF_8.toString).toLowerCase
        else
          letter.toString
      }

      extractFile(encoded)
    }.toVector
    val missingUrls = urls.filter { url =>
      url.startsWith(urlStart) && !sitemapUrls(url) && {
        true
      }
    }.toVector.distinct

    missingUrls.foreach(println)
  }
}
