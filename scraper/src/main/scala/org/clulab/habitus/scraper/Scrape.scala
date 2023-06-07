package org.clulab.habitus.scraper

import java.net.URL

case class Scrape(url: URL, titleOpt: Option[String], timestampOpt: Option[String], sourceOpt: Option[String], text: String) {
  def toText: String = {
    val source = sourceOpt.getOrElse("[none]")

    s"${titleOpt.get}\n\n${timestampOpt.get}\n\n$source\n\n\n$text"
  }

  def toJson: String = {
    ???
  }
}
