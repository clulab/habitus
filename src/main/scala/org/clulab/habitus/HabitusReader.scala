package org.clulab.habitus

import ai.lum.common.ConfigUtils._
import com.typesafe.config.ConfigFactory
import org.clulab.habitus.printer.{JsonlPrinter, MultiPrinter, TsvPrinter}
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


  def run(processor: GenericProcessor, inputDir: String, outputDir: String, threads: Int, printVariables: PrintVariables): Unit = {
    new File(outputDir).mkdir()

    def mkOutputFile(extension: String): String = outputDir + "/mentions" + extension

    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files

    new MultiPrinter(
      Lazy{new TsvPrinter(mkOutputFile(".tsv"))},
      Lazy(new JsonlPrinter(mkOutputFile(".jsonl")))
    ).autoClose { multiPrinter =>
      for (file <- parFiles) {
        try {
          val unfiltered = FileUtils.getTextFromFile(file)
          // fixme: temporary, simple text cleanup
          val text = unfiltered.replace("\n",
            " ").replace("- ", "")
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          val parsingResults = processor.parse(text)
          val doc = parsingResults.document
          val targetMentions = parsingResults.targetMentions
          multiPrinter.outputMentions(targetMentions, doc, filename, printVariables)
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
  }

}
