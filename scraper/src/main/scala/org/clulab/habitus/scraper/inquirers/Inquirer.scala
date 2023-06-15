package org.clulab.habitus.scraper.inquirers

import org.clulab.habitus.scraper.Page

import java.net.{URI, URL}

abstract class PageInquirer(val domain: String) {
  val scheme = "https"
  val fragment = null

  def inquire(inquiry: String, indexOpt: Option[Int] = None): Page = {
    val escapedInquiry = inquiry.replace(" ", "+")
    val (path, query) = indexOpt match {
      case None => ("/", s"s=$escapedInquiry")
      case Some(index) => (s"/page/$index/", s"s=$escapedInquiry")
    }
    val uri = new URI(scheme, domain, path, query, fragment)
    val url = uri.toURL.toString

    Page(url)
  }

  def matches(page: Page): Boolean = {
    val host = page.url.getHost

    // It is either the complete domain or a subdomain.
    // TODO: Change this!
    host == domain || host.endsWith("." + domain)
  }
}

class CorpusInquirer {
  val inquirers: Seq[PageInquirer] = Seq(
    new AdomOnlineInquirer(),
    new CitiFmOnlineInquirer(),
    new EtvGhanaInquirer(),
    new GhanaWebInquirer(),
    new GnaInquirer(),
    new HappyGhanaInquirer(),
    new TheChronicleInquirer(),
    new ThreeNewsInquirer()
  )

  def getPageInquirer(page: Page): PageInquirer = {
    val scraperOpt = inquirers.find(_.matches(page))

    scraperOpt.get
  }
}
