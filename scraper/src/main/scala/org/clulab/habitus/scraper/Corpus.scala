package org.clulab.habitus.scraper

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.scrapers.Scraper

import scala.util.Try

class Corpus(val pages: Seq[Page]) {

  def download(browser: Browser, baseDirName: String): Unit = {
    pages.foreach { page =>
      val scraper = Scraper.getScraper(page)
      val downloadTry = Try(scraper.downloadTo(browser, page, baseDirName))

      if (downloadTry.isFailure)
        println(s"Download of ${page.url.toString} failed!")
    }
  }

  def scrape(browser: Browser, baseDirName: String): Unit = {
    pages.foreach { page =>
      val scraper = Scraper.getScraper(page)

      scraper.scrapeTo(browser, page, baseDirName)
    }
  }
}

object Corpus {

  def apply(): Corpus = {
    def pages = Seq(
      // 1.1 - 1.14
      // This first one doesn't work with the %e2%82%b5 copied and pasted directly from the browser.
      Page("https://www.adomonline.com/galamseyer-fined-\u20B51-2-billion-for-illegal-mining/"),
      Page("https://www.adomonline.com/galamsey-worsened-because-of-you-ashanti-chiefs-call-out-politicians-over-illegal-mining/"),
      Page("https://www.adomonline.com/galamsey-14-regions-devastated-by-illegal-mining-activities-dr-solomon-owusu-reveals/"),
      Page("https://www.adomonline.com/stop-illegal-mining-take-advantage-of-govts-employment-programs-okyenhene-to-galamseyers/"),
      Page("https://www.adomonline.com/chinese-galamsey-queen-still-mining-illegally-inter-ministerial-taskforce-complains/"),

      Page("https://www.adomonline.com/well-fight-galamseyers-with-bulletproof-vest-and-weapons-abu-jinapor/"),
      Page("https://www.adomonline.com/small-scale-mining-fetched-ghana-almost-us1-2-billion-in-2022-minister/"),
      Page("https://www.adomonline.com/galamsey-i-feel-the-pains-of-ghanaians-stonebwoy/"),
      Page("https://www.adomonline.com/police-invite-frimpong-boateng-over-galamsey-report/"),
      Page("https://www.adomonline.com/galamsey-accident-nadmo-gives-update-on-remaining-trapped-miners/"),
      Page("https://www.adomonline.com/galamsey-pit-collapse-7-dead-only-5-rescued-alive-many-remain-trapped/"),
      Page("https://www.adomonline.com/babies-in-galamsey-areas-are-being-born-with-one-eye-without-genitalia-pathologist/"),
      Page("https://www.adomonline.com/galamsey-sub-chief-two-foreign-nationals-arrested-at-akyem-oda/"),
      Page("https://www.adomonline.com/galamsey-im-available-if-my-ideas-are-still-needed-prof-frimpong-boateng/"),

      // 2.1 - 2.10
      Page("https://citifmonline.com/2018/04/three-illegal-miners-die-galamsey-pit-bekwai/"),
      Page("https://citifmonline.com/2018/03/galamsey-operator-jailed-18-months-unlawful-escape/"),
      Page("https://citifmonline.com/2018/03/5-galamseyers-mining-with-cyanide-to-face-court-today/"),
      Page("https://citifmonline.com/2018/03/shoot-kill-galamseyers-suggestion-unfortunate-aning/"),
      Page("https://citifmonline.com/2018/03/mp-kicks-against-shoot-to-kill-proposal-in-galamsey-fight/"),

      Page("https://citifmonline.com/2018/03/er-2-pupils-drown-in-galamsey-pit-at-akyem-takorase/"),
      Page("https://citifmonline.com/2018/02/galamsey-threatens-ghanas-provision-of-water-researcher/"),
      Page("https://citifmonline.com/2018/02/punishment-for-galamseyers-must-be-deterrent-enough-media-coalition/"),
      Page("https://citifmonline.com/2018/02/armed-forces-intensify-anti-galamsey-fight-on-river-bodies/"),
      Page("https://citifmonline.com/2018/01/operation-vanguard-boss-laments-slow-prosecution-of-galamseyers/"),

      // 3.1 - 3.10
      Page("https://citifmonline.com/2017/12/ngo-plants-trees-to-reclaim-destroyed-galamsey-lands/"),
      Page("https://citifmonline.com/2017/07/ghana-science-associaton-joins-galamsey-fight/"),
      Page("https://citifmonline.com/2017/05/govt-will-sustain-fight-against-galamsey-western-regional-minister/"),
      Page("https://citifmonline.com/2017/05/stop-galamsey-start-farming-peasant-farmers-advocate/"),
      Page("https://citifmonline.com/2017/04/ghanas-cocoa-risk-international-boycott-over-galamsey-minister/"),

      Page("https://citifmonline.com/2017/04/the-wolves-in-sheeps-clothing-citi-fms-galamsey-campaign-in-retrospect/"),
      Page("https://citifmonline.com/2017/04/galamseyers-at-akrofusu-defy-governments-ultimatum/"),
      Page("https://citifmonline.com/2017/04/deport-foreign-nationals-involved-in-galamsey-group/"),
      Page("https://citifmonline.com/2017/04/govt-must-lead-efforts-to-end-galamsey-charismatic-churches/"),
      Page("https://citifmonline.com/2017/04/stopgalamseynow-selling-our-birth-right-in-the-guise-of-trade-article/"),

      // 4
      Page("https://citifmonline.com/2018/03/gold-rush-public-health-water-bodies-article/"),

      // 5.1 - 5.10
      Page("https://www.ghanaweb.com/GhanaHomePage/NewsArchive/Appoint-me-as-Water-Resources-Minister-and-I-ll-end-galamsey-in-two-weeks-Chief-1777016"),
      Page("https://www.ghanaweb.com/GhanaHomePage/regional/Illegal-miners-invade-cocoa-farms-at-Asante-Bekwai-District-Cocoa-Officer-laments-1776965"),
      Page("https://www.ghanaweb.com/GhanaHomePage/NewsArchive/We-need-stronger-hands-in-the-galamsey-fight-Advocate-1777313"),
      Page("https://www.ghanaweb.com/GhanaHomePage/regional/We-will-deal-harshly-with-ruthless-illegal-miners-in-Akrofuom-DCE-1777052"),
      Page("https://www.ghanaweb.com/GhanaHomePage/NewsArchive/Teshie-Residents-take-to-the-streets-over-dilapidated-roads-1776479"),
      Page("https://www.ghanaweb.com/GhanaHomePage/regional/Boy-12-drowns-in-abandoned-galamsey-pit-at-Denkyira-Gyaman-1774793"),
      Page("https://www.ghanaweb.com/GhanaHomePage/NewsArchive/Political-partisanship-greed-selfishness-making-galamsey-fight-difficult-Inusah-Fuseini-1774688"),
      Page("https://www.ghanaweb.com/GhanaHomePage/NewsArchive/Government-to-provide-bulletproof-vests-and-weapons-to-fight-galamseyers-Jinapor-1773986"),
      Page("https://www.ghanaweb.com/GhanaHomePage/features/Where-are-the-Nkrumahs-the-Mugabes-and-Gadafis-of-our-generation-The-menace-of-galamsey-in-Ghana-1774358"),
      Page("https://www.ghanaweb.com/GhanaHomePage/features/Are-galamseyers-and-Okada-operators-jumping-for-joy-over-Mahama-s-2024-candidacy-1773563")
    )

    new Corpus(pages)
  }
}
