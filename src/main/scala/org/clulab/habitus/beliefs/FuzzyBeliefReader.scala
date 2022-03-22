package org.clulab.habitus.beliefs

import org.clulab.habitus.utils._
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}

import java.io.{File, PrintWriter}
import scala.collection.mutable

// this just means I am extracting potential beliefs on very loose rules
object FuzzyBeliefReader {

  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToMap(args)
    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/test_input"
    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/test_output"
    val threads = 1//props.get("threads").map(_.toInt).getOrElse(1)

    run(inputDir, outputDir, threads)
  }

  def run(inputDir: String, outputDir: String, threads: Int): Unit = {
    new File(outputDir).mkdir()

    def mkOutputFile(extension: String): String = outputDir + "/mentions" + extension

    val vp = BeliefProcessor()
    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files


    val pw = new PrintWriter(mkOutputFile(".tsv"))

      for (file <- parFiles) {
        try {
          val unfiltered = FileUtils.getTextFromFile(file)
          // fixme: temporary, simple text cleanup
          val text = unfiltered.replace("\n",
            " ").replace("- ", "")
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          val (doc, mentions) = vp.parse(text)
          for (m <- mentions.filter(_.label matches "Belief")) println("m: " + m.text)
//          val printVars = PrintVariables("Belief", "believer", "belief")
//          val context = mutable.Map.empty[Int, ContextDetails]

//          multiPrinter.outputMentions(mentions, doc, filename, printVars)
          val beliefMentions = mentions.filter(_.label matches "Belief")
          for (b <- beliefMentions) {
            pw.println(s"${b.sentenceObj.getSentenceText}\t${b.text}")
          }
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
      }
    pw.close()
    }

}