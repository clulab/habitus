package org.clulab.habitus.apps.tpi

import ai.lum.common.FileUtils._
import org.clulab.processors.clu.CluProcessor
import org.clulab.utils.FileUtils

import java.io.File

object HedgeCountsApp extends App {
  val inputDirName = args.lift(0).getOrElse("../corpora/latinsquare/results")
  val processor = new CluProcessor()

  def toLines(text: String): Array[String] = {
    text.split('\n').map(_.trim).filter(_.nonEmpty)
  }

  def startWithoutOpt(text: String, startText: String): Option[String] = {
    if (text.startsWith(startText))
      Some(text.substring(startText.length))
    else
      None
  }

  val hedges = Set(
    "could",
    "likely",
    "may",
    "might",
    // "plausible",
    "possible",
    "potentially",
    "seems",
    // "speculative",
    "suggests",
    "unlikely",
    "would"
  )

  def process(file: File): Unit = {
    val text = FileUtils.getTextFromFile(file)
    val justifications = toLines(text)
      .flatMap(startWithoutOpt(_, "Justification:"))

    println(s"${file.getPath}")
    justifications.zipWithIndex.foreach { case (justification, index) =>
      val document = processor.mkDocument(justification)
      val sentences = document.sentences
      val count = sentences.count { sentence =>
        val words = sentence.words.map(_.toLowerCase)

        words.exists(hedges)
      }
      val length = sentences.length

      println(s"=$count/$length")
    }
  }

  val files = new File(inputDirName)
      .listFilesByWildcard("*.txt", recursive = true)
      .toVector.sortBy(_.getPath)
      .sorted

  files.foreach { file =>
    process(file)
  }
}
