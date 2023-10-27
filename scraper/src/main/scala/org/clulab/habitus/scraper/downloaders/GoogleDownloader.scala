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
import java.net.{URI, URL}
import java.nio.file.Files
import java.util.Properties
import scala.util.{Try, Using}

class GoogleDownloader extends GetPageDownloader(GoogleDomain) {
  val (searchEngineId, apiKey) = {
    val properties = new Properties()
    Using.resource(FileUtils.newBufferedInputStream("../google/google.properties")) { bufferedInputStream =>
      properties.load(bufferedInputStream)
    }

    (properties.getProperty(GoogleDownloader.SEARCH_ENGINE_ID), properties.getProperty(GoogleDownloader.API_KEY))
  }

  // This downloads the PDF pages from the external sites.
  def downloadExtern(page: Page, baseDirName: String): Unit = {
    val domain = page.url.getHost.split('.') /*.takeRight(2)*/.mkString(".") // Shorten to one .
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"

    Files.createDirectories(new File(subDirName).toPath)

    val file = cleaner.clean(page.url.getFile)
    val pdfFileName = file + ".pdf"
    val pdfLocationName = s"$subDirName/$pdfFileName"

    def getPdf(page: Page): Array[Byte] = {
      val url = page.url.getFile.substring(1) // Remove initial /
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
      val pdfTry = Try(getPdf(page))
      val pdf = pdfTry.getOrElse {
        // Before retry, wait for 3 seconds, which seems to help.
        Thread.sleep(3000)
        getPdf(page)
      }

      Using.resource(new FileOutputStream(pdfLocationName)) { fileOutputStream =>
        fileOutputStream.write(pdf)
      }
    }
  }

  // This downloads the JSON search results from Google.
  def downloadIntern(page: Page, baseDirName: String, inquiry: String): Unit = {
    val domain = page.url.getHost.split('.') /*.takeRight(2)*/.mkString(".") // Shorten to one .
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"

    Files.createDirectories(new File(subDirName).toPath)

    val dirtyFile = page.url.getFile.substring(1) // Remove initial /
    val file = cleaner.clean(dirtyFile)
    val jsonFileName = file + ".json"
    val jsonLocationName = s"$subDirName/$jsonFileName"
    val url = page.url.toString
        .replace(s"$${${GoogleDownloader.SEARCH_ENGINE_ID}}", searchEngineId)
        .replace(s"$${${GoogleDownloader.API_KEY}}", apiKey)

    def getJson(url: String): String = {
      val uri = Uri(new URI(url))
      val response = quickRequest.get(uri).send()
      val result = response.body

      result
    }

    if (!new File(jsonLocationName).exists) {
      // Use ProgressBar instead.
      // println(s"Downloading ${page.url.toString} to $htmlLocationName")
      val json = Try(getJson(url)).getOrElse {
        // Before retry, wait for 3 seconds, which seems to help.
        Thread.sleep(3000)
        getJson(url)
      }

      // Find out how many to write here.
      Using.resource(FileUtils.printWriterFromFile(jsonLocationName)) { printWriter =>
        printWriter.println(json)
      }
    }
  }

  override def download(browser: Browser, page: Page, baseDirName: String, inquiryOpt: Option[String] = None): Unit = {
    if (inquiryOpt.isEmpty) {
      val file = page.url.getFile

      if (file.startsWith("/http"))
        downloadExtern(page, baseDirName)
      else
        downloadIntern(page, baseDirName, "")
    }
    else
      downloadIntern(page, baseDirName, inquiryOpt.get)
  }
}

object GoogleDownloader {
  val SEARCH_ENGINE_ID = "SEARCH_ENGINE_ID"
  val API_KEY = "API_KEY"
}
