package org.clulab.habitus.scraper.inquirers

import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.GhanaWebDomain

class GhanaWebInquirer extends PageInquirer(GhanaWebDomain) {

  override def inquire(inquiry: String, indexOpt: Option[Int] = None): Page = {
    val url = indexOpt match {
      case None        => s"$scheme://$domain/GhanaHomePage/search.php"
      case Some(index) => s"$scheme://$domain/GhanaHomePage/search.php?page=$index"
    }

    Page(url)
  }
}
