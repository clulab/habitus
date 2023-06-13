package org.clulab.habitus.scraper

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.utils.FileUtils

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

    println(s"Downloading ${page.url.toString} to $htmlLocationName")

    val doc = browser.get(page.url.toString)
    val html = doc.toHtml

    Using.resource(FileUtils.printWriterFromFile(htmlLocationName)) { printWriter =>
      printWriter.println(html)
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
