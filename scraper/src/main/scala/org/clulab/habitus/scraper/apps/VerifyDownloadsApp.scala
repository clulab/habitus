package org.clulab.habitus.scraper.apps

import org.clulab.habitus.scraper.Cleaner
import org.clulab.utils.{FileUtils, Sourcer}

import java.net.URL
import scala.util.Using

object VerifyDownloadsApp extends App {
  val term = "sitemap"
  val corpusFileName = args.lift(0).getOrElse(s"./scraper/corpora/ghana/$term/articlecorpus-filtered.txt")
  val baseDirName = args.lift(1).getOrElse("/home/kwa/data/Corpora/habitus-project/corpora/ghana-sitemap/articlesonly/www_ghanaweb_com")
  val cleaner = new Cleaner()

  def extractFile(line: String): String = {
    val url = new URL(line)
    val file = cleaner.clean(url.getFile)  + ".html"

    file
  }

  val corpusFiles = Using.resource(Sourcer.sourceFromFilename(corpusFileName)) { source =>
    source.getLines.map(extractFile).toSet
  }

  FileUtils.findFiles(baseDirName, "html").foreach { file =>
    val name = file.getName

    if (!corpusFiles(name))
      println(s"$name is extra!")
  }
}
