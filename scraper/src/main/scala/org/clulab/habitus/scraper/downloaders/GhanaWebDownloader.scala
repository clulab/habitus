package org.clulab.habitus.scraper.downloaders

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.GhanaWebDomain
import org.clulab.utils.FileUtils

import java.io.File
import java.nio.file.Files
import scala.util.{Try, Using}

class GhanaWebDownloader extends PostPageDownloader(GhanaWebDomain) {

  override def download(browser: Browser, page: Page, baseDirName: String, inquiryOpt: Option[String] = None): Boolean = {
    val domain = page.url.getHost.split('.') /*.takeRight(2)*/ .mkString(".") // Shorten to one .
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"

    Files.createDirectories(new File(subDirName).toPath)

    val file = cleaner.clean(page.url.getFile)
    val htmlFileName = file + ".html"
    val htmlLocationName = s"$subDirName/$htmlFileName"

    if (new File(htmlLocationName).exists) false
    else {
      println(s"Downloading ${page.url.toString} to $htmlLocationName")

      val doc = if (inquiryOpt.isDefined) {
        val form: Map[String, String] = Map(
          "action" -> "1",
          "search_mode" -> "news", // or "general"
          // "cats[]" -> "G S B E O", // so this needs to be split up into multiple values
          "DATUM" -> "all",
          "search_type" -> "2", // for match all words or "2" for match exact phrase
          "Search" -> inquiryOpt.get
        )
        val doc = Try(browser.post(page.url.toString, form)).getOrElse {
          // The Scala interface doesn't seem to allow access to this.
          // Wait for 10 seconds if necessary.
          // Jsoup.connect(page.url.toString).timeout(10 * 1000).get()
          Thread.sleep(3000)
          browser.post(page.url.toString, form)
        }
        doc
      }
      else {
        val doc = Try(browser.get(page.url.toString)).getOrElse {
          // The Scala interface doesn't seem to allow access to this.
          // Wait for 10 seconds if necessary.
          // Jsoup.connect(page.url.toString).timeout(10 * 1000).get()
          Thread.sleep(3000)
          browser.get(page.url.toString)
        }
        doc
      }
      // The result contains "This page not found".  The access mechanism is more complicated.
      val html = doc.toHtml

      Using.resource(FileUtils.printWriterFromFile(htmlLocationName)) { printWriter =>
        printWriter.println(html)
      }
      true
    }
  }
}
