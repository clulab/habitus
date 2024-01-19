package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.GoogleDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.clulab.pdf2txt.Pdf2txt
import org.clulab.pdf2txt.languageModel.GigawordLanguageModel
import org.clulab.pdf2txt.preprocessor.{CasePreprocessor, LigaturePreprocessor, LineBreakPreprocessor, LinePreprocessor, NumberPreprocessor, ParagraphPreprocessor, UnicodePreprocessor, WordBreakByHyphenPreprocessor, WordBreakBySpacePreprocessor}
import org.clulab.utils.StringUtils

import scala.io.Codec
import org.clulab.pdf2txt.scienceparse.ScienceParseConverter
import org.clulab.pdf2txt.tika.TikaConverter
import org.clulab.utils.FileUtils
import org.json4s.DefaultFormats
import os.{proc => OSProc}

import java.io.File
import scala.util.Using

class GoogleArticleScraper extends PageArticleScraper(GoogleDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, pdfLocationName: String): ArticleScrape = {
    val rawText = GoogleArticleScraper.pdf2txt.read(new File(pdfLocationName), None)
    val text = GoogleArticleScraper.pdf2txt.process(rawText, GoogleArticleScraper.loops)
    val pdfMetadata = GoogleArticleScraper.readPdfMetadata(pdfLocationName)

    ArticleScrape(page.url, pdfMetadata.titleOpt, pdfMetadata.datelineOpt, pdfMetadata.bylineOpt, text)
  }

  def readPdf(page: Page, baseDirName: String): (String, String, String) = {
    val dirName = cleaner.clean(domain.domain)
    val subDirName = s"$baseDirName/$dirName"
    val file = cleaner.clean(page.url.getFile)

    val pdfFileName = file + ".pdf"
    val pdfLocationName = s"$subDirName/$pdfFileName"

    (subDirName, file, pdfLocationName)
  }

  override def scrapeTo(browser: Browser, page: Page, baseDirName: String): Unit = {
    val (subDirName, file, pdfLocationName) = readPdf(page, baseDirName)
    val scraped = scrape(browser, page, pdfLocationName)

    val txtFileName = file + ".txt"
    val txtLocationName = s"$subDirName/$txtFileName"
    val text = scraped.toText

    val jsonFileName = file + ".json"
    val jsonLocationName = s"$subDirName/$jsonFileName"
    val json = scraped.toJson

    Using.resource(FileUtils.printWriterFromFile(txtLocationName)) { printWriter =>
      printWriter.println(text)
    }

    Using.resource(FileUtils.printWriterFromFile(jsonLocationName)) { printWriter =>
      printWriter.println(json)
    }
  }
}

case class PdfMetadata(titleOpt: Option[String], datelineOpt: Option[String], bylineOpt: Option[String])

object GoogleArticleScraper {
  val loops = 1
  lazy val pdf2txt = {
    val pdfConverter = new ScienceParseConverter()
    // val pdfConverter = new TikaConverter()
    val languageModel = GigawordLanguageModel()
    val preprocessors = Array(
      new LinePreprocessor(),
      new ParagraphPreprocessor(),
      new UnicodePreprocessor(),
      new CasePreprocessor(),
      new NumberPreprocessor(),
      new LigaturePreprocessor(languageModel),
      new LineBreakPreprocessor(languageModel),
      new WordBreakByHyphenPreprocessor(),
      new WordBreakBySpacePreprocessor()
    )
    val pdf2txt = new Pdf2txt(pdfConverter, preprocessors)

    pdf2txt
  }

  def readPdfMetadata(pdfLocationName: String): PdfMetadata = {
    val commandResult = OSProc("pdfinfo", "-isodates", pdfLocationName.replace('/', File.separatorChar)).call(check = false)
    val pdfMetadata = {
      if (commandResult.exitCode != 0)
        PdfMetadata(None, None, None)
      else {
        val metaText = commandResult.out.text(Codec.UTF8)
        val lines = metaText.split('\n').map(_.trim)
        val map = lines.map { line =>
          val key = StringUtils.beforeFirst(line, ':', true)
          val value = StringUtils.afterFirst(line, ':', false).trim
          key -> value
        }.toMap
        val titleOpt = map.get("Title")
        val datelineOpt = map.get("ModDate").orElse(map.get("CreationDate"))
        val bylineOpt = map.get("Author")

        PdfMetadata(titleOpt, datelineOpt, bylineOpt)
      }
    }

    pdfMetadata
  }
}
