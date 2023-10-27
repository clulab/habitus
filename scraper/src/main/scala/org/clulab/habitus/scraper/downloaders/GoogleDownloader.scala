package org.clulab.habitus.scraper.downloaders

import org.clulab.habitus.scraper.domains.GoogleDomain
import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.utils.FileUtils
import sttp.client4.quick
import sttp.client4.quick._
import sttp.client4.Response
import sttp.model.Uri

import java.io.{File, FileOutputStream}
import java.net.URI
import java.nio.file.Files
import scala.util.{Try, Using}

class GoogleDownloader extends GetPageDownloader(GoogleDomain) {

  override def download(browser: Browser, page: Page, baseDirName: String, inquiryOpt: Option[String] = None): Unit = {
    val domain = page.url.getHost.split('.') /*.takeRight(2)*/.mkString(".") // Shorten to one .
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"

    Files.createDirectories(new File(subDirName).toPath)

    val dirtyFile = page.url.getFile.substring(1) // Remove initial /
    val file = cleaner.clean(dirtyFile)
    val pdfFileName = file + ".pdf"
    val pdfLocationName = s"$subDirName/$pdfFileName"

    def getPdf(url: String): Array[Byte] = {
      val uri = Uri(new URI(url))
      val response = basicRequest.get(uri).response(asByteArray).send()
      val byteArrayEither = response.body
      val result = byteArrayEither match {
        case Left(string) => throw new RuntimeException(string)
        case Right(byteArray) => byteArray
      }

      result
    }

    if (!new File(pdfLocationName).exists) {
      // Use ProgressBar instead.
      // println(s"Downloading ${page.url.toString} to $htmlLocationName")

      // Probably can't use Browser here.
      val pdf = Try(getPdf(dirtyFile)).getOrElse {
        // Before retry, wait for 3 seconds, which seems to help.
        Thread.sleep(3000)
        getPdf(dirtyFile)
      }

      Using.resource(new FileOutputStream(pdfLocationName)) { fileOutputStream =>
        fileOutputStream.write(pdf)
      }
    }
  }
}
