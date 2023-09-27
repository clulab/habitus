package org.clulab.habitus.apps.tpi

import ai.lum.common.FileUtils._
import org.clulab.utils.FileUtils

import java.io.File

class MessyText(val messy: String) {
  val clean = squeeze(uncapitalize(unquote(messy)))
  val summary = summarize(clean)

  def unquote(text: String): String = {
    text
        // Quotes
        .replace("\"", " ")
        .replace("\'", " ")
        .replace("\u201C", " ")
        .replace("\u201D", " ")
        .replace("\u2019", " ")
        // End of sentence punctuation
        .replace(".", " ")
        .replace(",", " ")
        // Internal punctuation
        .replace("-", " ")
        .replace(",", " ")
        // Typos
        .replace("sieze", "seize")
  }

  def uncapitalize(text: String): String = {
    text.toLowerCase
  }

  def tighten(text: String): String = {

    def loop(text: String): String = {
      val tightened = text.replace("  ", " ")

      if (text.length == tightened.length) tightened
      else loop(tightened)
    }

    loop(text)
  }

  def summarize(text: String): String = {
    val result = text.take(text.length * 6 / 10)

    result
  }

  def squeeze(text: String): String = text.replace(" ", "")

  def isEmpty: Boolean = clean.isEmpty

  override def hashCode(): Int = summary.hashCode

  override def equals(other: Any): Boolean = {

//    if (this.messy.contains("revival")) {
//      if (other.asInstanceOf[MessyText].messy.contains("revival")) {
//        println("These may be equal.")
//      }
//    }

    val result = Some(other)
        .collect { case other: MessyText => this.summary == other.summary }
        .contains(true)

//    if (result)
//      println("Duplicate!")
    result
  }
}

object CitationCounts extends App {
  val inputDirName = args.lift(0).getOrElse("../corpora/chatgpt/report")

  def betweenOrAfter(text: String, startText: String, endText: String, altStartText: String): String = {
    val (start, end) =  {
      if (text.indexOf(startText) >= 0) {
        val start = text.indexOf(startText) + startText.length
        val end = text.indexOf(endText)

        (start, end)
      }
      else if (text.indexOf(altStartText) >= 0) {
        val start = text.indexOf(altStartText) + altStartText.length
        val end = text.length

        (start, end)
      }
      else
        (-1, -1)
    }

    text.slice(start, end).trim
  }

  def toLines(text: String): Array[String] = {
    text.split('\n').map(_.trim).filter(_.nonEmpty)
  }

  def startWithoutOpt(text: String, startText: String): Option[String] = {
    if (text.startsWith(startText))
      Some(text.substring(startText.length))
    else
      None
  }

  def process(file: File): Unit = {
    val text = FileUtils.getTextFromFile(file)
    val citations = {
      val context = betweenOrAfter(text, "CONTEXT SENTENCES:", "ANSWER:", "CONTEXT:")
      val citations = toLines(context).map(new MessyText(_)).filterNot(_.isEmpty)
      val distinct = citations.distinct

      if (citations.length != distinct.length) {
        val groups = citations.groupBy(_.clean)
        val duplicates = groups.filter(_._2.length > 1)

//        println(duplicates)
      }

      distinct
    }
    val justifications = toLines(text)
      .flatMap(startWithoutOpt(_, "Justification:"))
      .map(new MessyText(_))

    justifications.zipWithIndex.foreach { case (justification, index) =>
      val quotes = citations.filter { citation =>
        justification.clean.contains(citation.summary)
      }
      val count = quotes.length
      val choice = ('A' + index).toChar

      println(s"${file.getPath}\t$choice\t$count")
    }
  }

  val files = new File(inputDirName).listFilesByWildcard("*.txt", recursive = true).toVector.sortBy(_.getPath)

  files.foreach { file =>
    process(file)
  }
}
