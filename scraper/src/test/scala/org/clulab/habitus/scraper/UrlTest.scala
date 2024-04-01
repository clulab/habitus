package org.clulab.habitus.scraper

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}

import java.net.{URLDecoder, URLEncoder}

class UrlTest extends Test {

  behavior of "BOM in URL"

  it should "download" in {
//    val url = "https://www.adomonline.com/%ef%bb%bfabout29-billion-needed-to-reclaim-degradable-land-minister/"
//    val url = "https://thechronicle.com.gh/former-banker-charged-with-gh%c2%a272750-fraud/"
    val url = "https://thechronicle.com.gh/fidelity-bank-donates-gh%c2%a2110k-to-design-technology-institute/"
//    val url = "https://thechronicle.com.gh/former-banker-charged-with-gh%25c2%25a272750-fraud/"
//    val url = "https://www.adomonline.com/\ufeffabout29-billion-needed-to-reclaim-degradable-land-minister/"
    val browser: Browser = JsoupBrowser()
    val decodedUrl = URLDecoder.decode(url, "utf-8")
    val doc = browser.get(decodedUrl)
    val html = doc.toHtml

    println(html)
  }
}
