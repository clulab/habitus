package org.clulab.habitus.scraper

import org.clulab.habitus.scraper.domains.Domain

class DomainSpecific(val domain: Domain) {

  def matches(page: Page): Boolean = {
    val host = page.url.getHost
    val protocol = page.url.getProtocol
    val file = page.url.getFile

    host.endsWith(domain.domain) && protocol.startsWith(domain.protocol) && file.endsWith(domain.extension)
  }
}
