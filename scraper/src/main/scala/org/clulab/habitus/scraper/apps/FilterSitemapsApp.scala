package org.clulab.habitus.scraper.apps

import org.clulab.utils.{FileUtils, Sourcer, StringUtils}

import scala.util.Using

object FilterSitemapsApp extends App {
  val urlString = args.lift(0).getOrElse("https://adomonline.com")
  val inFileName = args.lift(1).getOrElse("../corpora/pages/" + StringUtils.afterLast(urlString, '/') + ".pages")
  val outFileName = inFileName + "-1000.out"

  def adomOnlineFilter(line: String): Boolean = { true &&
    line.endsWith("/") && // Articles look like a directory.
    !line.startsWith("https://www.adomonline.com/tag/") && // We're not interested in tags.
    !line.startsWith("https://www.adomonline.com/author/") && // We're not interested in authors.
    !line.startsWith("https://www.adomonline.com/category/") && // Ditto with categories.
    (line.count(_ == '/') == 4) && // We need no subdirectory at all.  TODO: this works for many
    (line != "https://www.adomonline.com/") && // Skip the home page.
    !line.startsWith("https://www.adomonline.com/__trashed") && // These appear to have been deleted.
    !line.endsWith("-fm/") &&
    !line.endsWith("-fm-live/") &&
    !line.endsWith("-live-radio/") &&
    !line.endsWith("-tv-live/") &&
    line.contains('-') &&
    !Seq(
      "https://www.adomonline.com/20200210121612-nyinsenneawuo/",
      "https://www.adomonline.com/20200407182050-kenkanme/",
      "https://www.adomonline.com/20191001210623-wiasemunsem-200x200/",
      "https://www.adomonline.com/adomonline-com-newsletter-survey/",
      "https://www.adomonline.com/joy-learning/",
      "https://www.adomonline.com/privacy-policy/",
      "https://www.adomonline.com/adom-podcasts/",
      "https://www.adomonline.com/adom-tv-live/",
      "https://www.adomonline.com/election-headquarters/",
      "https://www.adomonline.com/contact-us/"
    ).contains(line) &&
    true
  }

  def threeNewsFilter(line: String): Boolean = { true &&
    line.endsWith("/") && // Articles look like a directory.
    line.startsWith("https://3news.com/") && // We're not interested in tags.
    !line.startsWith("https://3news.com/tag/") && // We're not interested in tags.
    !line.startsWith("https://3news.com/playlist/") && // We're not interested in tags.
    (line.count(_ == '/') == 5) && // We need no subdirectory at all.  TODO: this works for many
    line.contains('-') &&
    !Seq(
      "https://3news.com/showbiz/art-design/"
    ).contains(line) &&
    true
  }

  Using.resources(
    FileUtils.printWriterFromFile(outFileName),
    Sourcer.sourceFromFilename(inFileName)
  ) { (printWriter, source) =>
    val lines = source.getLines
    val filteredLines = lines.filter(adomOnlineFilter).take(1000)

    filteredLines.foreach { line =>
      val newLine = line.dropRight(1)

      printWriter.println(newLine)
      println(newLine)
    }
  }
}
