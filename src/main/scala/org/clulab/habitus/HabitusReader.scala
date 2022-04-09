package org.clulab.habitus

import ai.lum.common.ConfigUtils._
import com.typesafe.config.ConfigFactory
import org.clulab.habitus.printer.{DynamicArgsTsvPrinter, JsonlPrinter, MultiPrinter, TsvPrinter}
import org.clulab.habitus.utils._
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}

import java.io.File

class HabitusReader() extends App {
  val config = ConfigFactory.load()
  val inputDir: String = config[String]("inputDir")
  val outputDir: String = config[String]("outputDir")
  val threads: Int = config[Int]("threads")
  val factuality: Boolean = config[Boolean]("factuality")
  val mentions = "/mentions"

  def run(processor: GenericProcessor, inputDir: String, outputDir: String, threads: Int): Unit = {
    new File(outputDir).mkdirs()

    def mkOutputFile(extension: String): String = outputDir + mentions + extension

    // fixme: temporary, simple text cleanup
    def cleanText(text: String): String = text
        .replace("\n", " ")
        .replace("- ", "")

    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files

    new MultiPrinter(
      Lazy{new TsvPrinter(mkOutputFile(".tsv"))},
      Lazy(new JsonlPrinter(mkOutputFile(".jsonl"))),
      Lazy(new DynamicArgsTsvPrinter(outputDir + mentions + "-", ".tsv"))
    ).autoClose { multiPrinter =>
      for (file <- parFiles) {
        try {
          val unfiltered = FileUtils.getTextFromFile(file)
          val text = cleanText(unfiltered)
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          val parsingResults = processor.parse(text)
          val doc = parsingResults.document
          val targetMentions = parsingResults.targetMentions
          multiPrinter.outputMentions(targetMentions, filename)
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
  }
}
