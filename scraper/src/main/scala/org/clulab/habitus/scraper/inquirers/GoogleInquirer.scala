package org.clulab.habitus.scraper.inquirers

import org.clulab.habitus.scraper.domains.GoogleDomain
import org.clulab.habitus.scraper.{DomainSpecific, Page}

import java.net.{URI, URL}

class GoogleInquirer extends PageInquirer(GoogleDomain) {

  override def inquire(inquiry: String, indexOpt: Option[Int] = None, templatePageOpt: Option[Page]): Page = {
    if (templatePageOpt.isEmpty)
      super.inquire(inquiry, indexOpt, templatePageOpt)
    else {
      val index = indexOpt.getOrElse(0)
      val startIndex = 1 + (index - 1) * GoogleInquirer.PER_PAGE
      val url = s"${templatePageOpt.get.url.toString}&start=$startIndex"

      Page(url)
    }
  }
}

object GoogleInquirer {
  val PER_PAGE = 10
}
