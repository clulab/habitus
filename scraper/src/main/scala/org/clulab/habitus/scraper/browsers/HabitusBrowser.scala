package org.clulab.habitus.scraper.browsers

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.jsoup.Connection

class HabitusBrowser extends JsoupBrowser {

  override def requestSettings(conn: Connection) = conn.timeout(HabitusBrowser.customTimeout)
}

object HabitusBrowser {
  val customTimeout = 30000
}
