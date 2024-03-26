package org.clulab.habitus.scraper.browsers

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import org.jsoup.Connection

import java.net.URLDecoder

class HabitusBrowser extends JsoupBrowser {

  override def get(url: String): JsoupDocument = {
    val decodedUrl = URLDecoder.decode(url, "utf-8")

    if (url != decodedUrl)
      println("Watch this!")
    super.get(decodedUrl)
  }

  override def requestSettings(conn: Connection) = conn
      .timeout(HabitusBrowser.customTimeout)
      // This might be necessary for some sites.
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0")
}

object HabitusBrowser {
  val customTimeout = 30000
}
