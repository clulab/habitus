package org.clulab.habitus.beliefs

import org.clulab.habitus.utils.{ContextDetails, JsonlPrinter, Lazy, MultiPrinter, PrintVariables, TsvPrinter}
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

  def run(inputDir: String, outputDir: String, threads: Int): Unit = {
    new File(outputDir).mkdir()

    def mkOutputFile(extension: String): String = outputDir + "/mentions" + extension

    val vp = BeliefProcessor()
    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files

    new MultiPrinter(
      Lazy(new TsvPrinter(mkOutputFile(".tsv"))),
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
          val (doc, expandedMentions, beliefMentions) = vp.parse(text)
          val printVars = PrintVariables("Belief", "believer", "belief")
          val context = mutable.Map.empty[Int, ContextDetails]

          multiPrinter.outputMentions(beliefMentions, doc, filename, printVars)
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
  }
}