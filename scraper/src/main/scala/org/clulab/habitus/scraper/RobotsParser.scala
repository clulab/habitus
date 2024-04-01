package org.clulab.habitus.scraper

import scala.io.Source

class RobotsParser {

  def parse(text: String): Seq[String] = {
    val localSitemapIndexes = Source
        .fromString(text).getLines().flatMap { line =>
          if (line.startsWith(RobotsParser.header))
            Some(line.drop(RobotsParser.header.length).trim)
          else
            None
        }
        .toVector
        .map { sitemapIndex =>
          sitemapIndex.replaceAll("/www.gna.org.gh/", "/gna.org.gh/")
        }
        .map { sitemapIndex =>
          sitemapIndex.replaceAll("/.xml", "/sitemap.xml")
        }

    localSitemapIndexes
  }
}

object RobotsParser {
  val header = "Sitemap:"
}
