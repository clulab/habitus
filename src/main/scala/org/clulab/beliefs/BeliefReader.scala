package org.clulab.beliefs

import org.clulab.habitus.utils.{ContextDetails, JsonlPrinter, PrintVariables, TsvPrinter}
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}
import org.clulab.utils.Closer.AutoCloser

import java.io.File
import scala.collection.mutable

object BeliefReader {

  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToMap(args)
    val inputDir = props("in")
    val outputDir = props("out")
    val threads = props.get("threads").map(_.toInt).getOrElse(1)

    run(inputDir, outputDir, threads)
  }

  def run(inputDir: String, outputDir: String, threads: Int) {
    new File(outputDir).mkdir()
    val tsvOutputFile = outputDir + "/mentions.tsv"
    val jsonlOutputFile = outputDir + "/mentions.jsonl"

    val vp = BeliefProcessor()
    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files

    new JsonlPrinter(jsonlOutputFile).autoClose { jsonlPrinter =>
      new TsvPrinter(tsvOutputFile).autoClose { tsvPrinter =>
        for (file <- parFiles) {
          try {
            val text = FileUtils.getTextFromFile(file)
            val filename = StringUtils.afterLast(file.getName, '/')
            println(s"going to parse input file: $filename")
            val (doc, mentions) = vp.parse(text)
            val printVars = PrintVariables("Belief", "believer", "belief")
            val context = mutable.Map.empty[Int, ContextDetails]
            synchronized {
              tsvPrinter.outputMentions(mentions, doc, context, filename, printVars)
              jsonlPrinter.outputMentions(mentions, doc, context, filename, printVars)
            }
          }
          catch {
            case e: Exception => e.printStackTrace()
          }
        }
      }
    }
  }
}