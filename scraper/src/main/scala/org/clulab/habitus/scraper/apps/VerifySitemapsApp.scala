package org.clulab.habitus.scraper.apps

import org.clulab.utils.{Sourcer, StringUtils}
import org.clulab.wm.eidoscommon.utils.TsvReader

import java.net.{URL, URLEncoder}
import java.nio.charset.StandardCharsets
import scala.util.Using

object VerifySitemapsApp extends App {
  val urlString = args.lift(0).getOrElse("https://adomonline.com")
  val sitemapFileName = args.lift(1).getOrElse("../corpora/pages/" + StringUtils.afterLast(urlString, '/') + ".pages")
  val corpusFileName = "../corpora/ghana/dataset55k.tsv"
  val tsvReader = new TsvReader()
  val urlStart = "https://www.adomonline.com/"

  val sitemapUrls = Using.resource(Sourcer.sourceFromFilename(sitemapFileName)) { source =>
    source.getLines.toSet
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

      encoded
    }
    val missingUrls = urls.filter { url =>
      url.startsWith(urlStart) && !sitemapUrls(url) && {
        !url.startsWith("https://www.adomonline.com/__trashed") // &&
        // !url.contains('%')
      }
    }.toVector.distinct

    // The one file shown now gives a 404 at the site.
    missingUrls.foreach(println)
  }
}
