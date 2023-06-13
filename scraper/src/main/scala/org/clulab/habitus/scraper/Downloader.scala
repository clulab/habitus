package org.clulab.habitus.scraper

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.utils.FileUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.DocumentType

import java.io.File
import java.nio.file.Files
import scala.util.{Try, Using}

class PageDownloader() {
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
        // Wait for 10 seconds if necessary.
        // Jsoup.connect(page.url.toString).timeout(10 * 1000).get()
        browser.get(page.url.toString)
      }
      val html = doc.toHtml

      Using.resource(FileUtils.printWriterFromFile(htmlLocationName)) { printWriter =>
        printWriter.println(html)
      }
    }
  }
}

class CorpusDownloader(val corpus: Corpus) {
  val pageDownloader = new PageDownloader()

  def download(browser: Browser, baseDirName: String): Unit = {
    corpus.lines.foreach { line =>
      val page = Page(line)
      val downloadTry = Try(pageDownloader.download(browser, page, baseDirName))

      if (downloadTry.isFailure)
        println(s"Download of ${page.url.toString} failed!")
    }
  }
}
