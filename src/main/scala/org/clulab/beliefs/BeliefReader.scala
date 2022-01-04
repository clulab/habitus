package org.clulab.beliefs

import org.clulab.habitus.utils.{ContextDetails, JsonlPrinter, Lazy, MultiPrinter, PrintVariables, TsvPrinter}
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}
import org.clulab.utils.Closer.AutoCloser

import java.io.File
import scala.collection.mutable

object BeliefReader {

  def main(args: Array[String]): Unit = {
//    val props = StringUtils.argsToMap(args)
//    val inputDir = props("in")
//    val outputDir = props("out")
//    val threads = props.get("threads").map(_.toInt).getOrElse(1)
    val inputDir = "/Users/alexeeva/Desktop/habitus_related/docs_i_downloaded_output"
//    val inputDir = "/Users/alexeeva/Downloads/Masha-Hubert fr-en-txt SAED BULLITINS/en-txt"
    val outputDir = "/Users/alexeeva/Desktop/habitus_related/docs_i_downloaded_extracted_beliefs_dec14"
//    val outputDir = "/Users/alexeeva/Downloads/Masha-Hubert fr-en-txt SAED BULLITINS/en-txt/output"
    run(inputDir, outputDir, 1)
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

          val text1 = FileUtils.getTextFromFile(file)
          val text = text1.replace("\n", "" +
            " ").replace("- ", "")
          println(f"======\n${text1}\n++++++\n${text}\n-------")
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          val (doc, mentions) = vp.parse(text)
          val printVars = PrintVariables("Belief", "believer", "belief")
          val context = mutable.Map.empty[Int, ContextDetails]

          multiPrinter.outputMentions(mentions, doc, context, filename, printVars)
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
  }
}