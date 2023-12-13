package org.clulab.habitus.scraper

import org.clulab.habitus.scraper.domains.Domain

class DomainSpecific(val domain: Domain) {

  def matches(page: Page): Boolean = {
    val host = page.url.getHost
    val protocol = page.url.getProtocol

    host.endsWith(domain.domain) && protocol.startsWith(domain.protocol)
  }
}
