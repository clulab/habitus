package org.clulab.habitus.scraper.downloaders

import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.GnaDomain

class GnaDownloader extends GetPageDownloader(GnaDomain) {

  override def isValidPage(page: Page): Boolean = {
    val urlName = page.url.toString

    !urlName.contains("--")
  }
}
