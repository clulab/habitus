package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.GoogleDomain
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.clulab.pdf2txt.Pdf2txt
import org.clulab.pdf2txt.common.utils.MetadataHolder
import org.clulab.pdf2txt.languageModel.GigawordLanguageModel
import org.clulab.pdf2txt.preprocessor.{CasePreprocessor, LigaturePreprocessor, LineBreakPreprocessor, LinePreprocessor, NumberPreprocessor, ParagraphPreprocessor, UnicodePreprocessor, WordBreakByHyphenPreprocessor, WordBreakBySpacePreprocessor}
// import org.clulab.pdf2txt.scienceparse.ScienceParseConverter
import org.clulab.pdf2txt.tika.TikaConverter
import org.clulab.utils.FileUtils
import org.json4s.DefaultFormats

import java.io.File
import scala.util.Using

class GoogleArticleScraper extends PageArticleScraper(GoogleDomain) {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def scrape(browser: Browser, page: Page, pdfLocationName: String): ArticleScrape = {
    val metadataHolder = new MetadataHolder()
    val rawText = GoogleArticleScraper.pdf2txt.read(new File(pdfLocationName), Some(metadataHolder))
    val text = GoogleArticleScraper.pdf2txt.process(rawText, GoogleArticleScraper.loops)

    ArticleScrape(page.url, titleOpt = None, datelineOpt = None, bylineOpt = None, text)
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

object GoogleArticleScraper {
  val loops = 1
  lazy val pdf2txt = {
    // In order to use ScienceParse, the jars must be included directly in this project,
    // in the lib directory, because they aren't published properly to maven.
    // val pdfConverter = new ScienceParseConverter()
    // On the other hand, Tika seems to require Java 11.
    val pdfConverter = new TikaConverter()
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
}
