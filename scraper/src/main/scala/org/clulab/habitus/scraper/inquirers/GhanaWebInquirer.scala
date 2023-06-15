package org.clulab.habitus.scraper.inquirers

import org.clulab.habitus.scraper.Page

class GhanaWebInquirer extends PageInquirer("www.ghanaweb.com") {

  override def inquire(inquiry: String, indexOpt: Option[Int] = None): Page = {
    val url = indexOpt match {
      case None        => s"$protocol://$domain/GhanaHomePage/search.php"
      case Some(index) => s"$protocol://$domain/GhanaHomePage/search.php?page=$index"
    }

    Page(url)
  }
}
