package org.clulab.habitus.scraper.inquirers

import org.clulab.habitus.scraper.domains.Domain
import org.clulab.habitus.scraper.{DomainSpecific, Page}

import java.net.{URI, URL}

abstract class PageInquirer(domain: Domain) extends DomainSpecific(domain) {
  val scheme = "https"
  val fragment = null

  def inquire(inquiry: String, indexOpt: Option[Int] = None, templatePageOpt: Option[Page] = None): Page = {
    val escapedInquiry = inquiry.replace(" ", "+")
    val (path, query) = indexOpt match {
      case None => ("/", s"s=$escapedInquiry")
      case Some(index) => (s"/page/$index/", s"s=$escapedInquiry")
    }
    val uri = new URI(scheme, domain.domain, path, query, fragment)
    val url = uri.toURL.toString

    Page(url)
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
    new ThreeNewsInquirer(),
    new GoogleInquirer()
  )

  def getPageInquirer(page: Page): PageInquirer = {
    val scraperOpt = inquirers.find(_.matches(page))

    scraperOpt.get
  }
}
