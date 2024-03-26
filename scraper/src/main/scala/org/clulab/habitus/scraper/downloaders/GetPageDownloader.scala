package org.clulab.habitus.scraper.downloaders

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.domains.Domain
import org.clulab.habitus.scraper.{Cleaner, DomainSpecific, Page}
import org.clulab.utils.FileUtils

import java.io.{File, UnsupportedEncodingException}
import java.nio.file.Files
import scala.util.{Try, Using}

class GetPageDownloader(domain: Domain) extends PageDownloader(domain) {
  val cleaner = new Cleaner()

  def download(browser: Browser, page: Page, baseDirName: String, inquiryOpt: Option[String] = None): Boolean = {
    val domain = page.url.getHost.split('.')/*.takeRight(2)*/.mkString(".") // Shorten to one .
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"

    Files.createDirectories(new File(subDirName).toPath)

    val file = cleaner.clean(page.url.getFile) // This may automatically take off the #comment.
    val htmlFileName = file + ".html" // This is added even if it already ended in .html.
    val htmlLocationName = s"$subDirName/$htmlFileName"

    if (new File(htmlLocationName).exists) false
    else {
      // Use ProgressBar instead.
      // println(s"Downloading ${page.url.toString} to $htmlLocationName")

      val tryDoc = Try(browser.get(page.url.toString))
      val doc =
        if (tryDoc.isSuccess) tryDoc.get
        else {
          // If failed with encoding error, then don't wait.
          val throwable = tryDoc.failed.get

          if (!throwable.isInstanceOf[UnsupportedEncodingException])
            // Before retry, wait for 3 seconds, which seems to help.
            Thread.sleep(3000)
        browser.get(page.url.toString)
      }
      val html = doc.toHtml

      Using.resource(FileUtils.printWriterFromFile(htmlLocationName)) { printWriter =>
        printWriter.println(html)
      }
      true
    }
  }
}
