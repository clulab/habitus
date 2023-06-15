package org.clulab.habitus.scraper.inquirers

import org.clulab.habitus.scraper.Page

abstract class PageInquirer(val domain: String) {
  val protocol = "https"
  def inquire(inquiry: String, indexOpt: Option[Int] = None): Page = {
    val url = indexOpt match {
      case None => s"$protocol://$domain/?s=$inquiry"
      case Some(index) => s"$protocol://$domain/page/$index/?s=$inquiry"
    }

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
