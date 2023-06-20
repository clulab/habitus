package org.clulab.habitus.scraper.downloaders

import org.clulab.habitus.scraper.domains.Domain
import org.clulab.habitus.scraper.{Cleaner, Page}

abstract class PostPageDownloader(domain: Domain) extends PageDownloader(domain) {
  val cleaner = new Cleaner()
}
