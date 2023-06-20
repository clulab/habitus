package org.clulab.habitus.scraper.downloaders

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.domains.Domain
import org.clulab.habitus.scraper.{Cleaner, Page}
import org.clulab.utils.FileUtils

import java.io.File
import java.nio.file.Files
import scala.util.{Try, Using}

class PostPageDownloader(domain: Domain) extends PageDownloader(domain) {
  val cleaner = new Cleaner()

  def download(browser: Browser, page: Page, baseDirName: String): Unit = {
    val domain = page.url.getHost.split('.')/*.takeRight(2)*/.mkString(".") // Shorten to one .
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"

    Files.createDirectories(new File(subDirName).toPath)

    val file = cleaner.clean(page.url.getFile)
    val htmlFileName = file + ".html"
    val htmlLocationName = s"$subDirName/$htmlFileName"

    if (!new File(htmlLocationName).exists) {
      println(s"Downloading ${page.url.toString} to $htmlLocationName")

      val doc = Try(browser.get(page.url.toString)).getOrElse {
        // The Scala interface doesn't seem to allow access to this.
        // Wait for 10 seconds if necessary.
        // Jsoup.connect(page.url.toString).timeout(10 * 1000).get()
        Thread.sleep(3000)
        browser.get(page.url.toString)
      }
      val html = doc.toHtml

      Using.resource(FileUtils.printWriterFromFile(htmlLocationName)) { printWriter =>
        printWriter.println(html)
      }
    }
  }
}
