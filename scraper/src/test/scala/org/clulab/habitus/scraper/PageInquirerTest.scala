package org.clulab.habitus.scraper

import org.clulab.habitus.scraper.inquirers.AdomOnlineInquirer

import java.net.URLEncoder

class PageInquirerTest extends Test {

  behavior of "PageInquirer"

  it should "construct proper single-word inquiry" in {
    val inquirer = new AdomOnlineInquirer()
    val inquiry = "galamsey"
    val page0 = inquirer.inquire(inquiry)

    page0.url.toString should be ("https://www.adomonline.com/?s=galamsey")

    val page1 = inquirer.inquire(inquiry, Some(1))

    page1.url.toString should be ("https://www.adomonline.com/page/1/?s=galamsey")
  }

  it should "construct proper multi-word inquiry" in {
    val inquirer = new AdomOnlineInquirer()
    val inquiry = "illegal mining"
    val page0 = inquirer.inquire(inquiry)

    page0.url.toString should be("https://www.adomonline.com/?s=illegal+mining")

    val page1 = inquirer.inquire(inquiry, Some(1))

    page1.url.toString should be("https://www.adomonline.com/page/1/?s=illegal+mining")
  }
}
