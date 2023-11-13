package org.clulab.habitus.scraper.scrapes

import org.json4s.JsonDSL._
import org.json4s.jackson.{prettyJson, renderJValue}
import org.json4s.{DefaultFormats, Formats}

import java.net.URL

case class ArticleScrape(url: URL, titleOpt: Option[String], datelineOpt: Option[String], bylineOpt: Option[String], text: String) {
  implicit val formats: Formats = DefaultFormats

  def toText: String = {
    val title = titleOpt.getOrElse(ArticleScrape.none)
    val dateline = datelineOpt.getOrElse(ArticleScrape.none)
    val byline = bylineOpt.getOrElse(ArticleScrape.none)

    s"$title\n\n$dateline\n\n$byline\n\n\n$text"
  }

  def toJson: String = {
    val jObject =
        ("url" -> url.toString) ~
        ("title" -> titleOpt) ~
        ("dateline" -> datelineOpt) ~
        ("byline" -> bylineOpt) ~
        ("text" -> text)
    val json = prettyJson(renderJValue(jObject))

    json
  }
}

object ArticleScrape {
  val none = "[none]"
}
