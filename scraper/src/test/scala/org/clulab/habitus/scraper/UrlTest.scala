package org.clulab.habitus.scraper

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}

class UrlTest extends Test {

  behavior of "BOM in URL"

  ignore should "download" in {
//  val url = "https://www.adomonline.com/%ef%bb%bfabout29-billion-needed-to-reclaim-degradable-land-minister/"
    val url = "https://www.adomonline.com/\ufeffabout29-billion-needed-to-reclaim-degradable-land-minister/"
    val browser: Browser = JsoupBrowser()
    val doc = browser.get(url)
    val html = doc.toHtml

    println(html)
  }
}
