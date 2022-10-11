package org.clulab.habitus.beliefs

import org.clulab.habitus.utils.{ContextDetails, JsonlPrinter, Lazy, MultiPrinter, PrintVariables, TsvPrinter}
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}
import org.clulab.utils.Closer.AutoCloser

import java.io.{File, PrintWriter}
import scala.collection.mutable

object BeliefReader {

  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToMap(args)
//    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/Rice_growing_in_Senegal_River_Valley/txt"//props("in")
//    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/Rice_growing_in_Senegal_River_Valley/output"//props("out")
//    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/interviews"
//    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/interviews/output"
//    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/bangladesh-vs-5others-processed-by-ik/txt"
//    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/bangladesh-vs-5others-processed-by-ik/output"

    val inputDir = "/home/alexeeva/Repos/habitus-utils/end_to_end_google_api_scrapper/qatar-social-norms/plain_text"
    val outputDir = "/home/alexeeva/Repos/habitus-utils/end_to_end_google_api_scrapper/qatar-social-norms/belief-mentions"

    val threads = 1 //props.get("threads").map(_.toInt).getOrElse(1)

    run(inputDir, outputDir, threads)
  }

  def run(inputDir: String, outputDir: String, threads: Int): Unit = {
    new File(outputDir).mkdir()

    def mkOutputFile(extension: String): String = outputDir + "/mentions" + extension

    val vp = BeliefProcessor()
    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files
//    new PrintWriter(new File(outputDir + "/mentions.tsv")).autoClose { pw =>
//      pw.println("filename\tmention type\tfound by\tsentence\tmention text\targs in all next columns (argType: argText)")
      for (file <- parFiles) {
        val pw = new PrintWriter(new File(outputDir + "/mentions-" + file.getName + ".tsv"))
        try {
          val unfiltered = FileUtils.getTextFromFile(file)
          // fixme: temporary, simple text cleanup
          val text = unfiltered.replace("\n",
            " ").replace("- ", "")
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          val (_, mentions) = vp.parse(text)
          val contentMentions = mentions.filter(m => m.label matches "Belief")//m.labels.contains("Event") & !m.isInstanceOf[TextBoundMention])
          for (m <- contentMentions) {
            println(m.label + " " + m.text + " " + m.foundBy)
            pw.print(s"${filename}\t${m.label}\t${m.foundBy}\t${m.sentenceObj.getSentenceText}\t${m.text}")
            for ((key, values) <- m.arguments) {
              if (values.nonEmpty) {
                // multiple args of same type are "::"-separated
                val value = values.map(_.text.trim().replace("\t", "")).mkString("::")
                pw.print(s"\t$key:\t$value")
              }
            }
            pw.println()
          }
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
        pw.close()
      }
    }
//    new MultiPrinter(
//      Lazy(new TsvPrinter(mkOutputFile(".tsv"))),
//      Lazy(new JsonlPrinter(mkOutputFile(".jsonl")))
//    ).autoClose { multiPrinter =>
//      for (file <- parFiles) {
//        try {
//          val unfiltered = FileUtils.getTextFromFile(file)
//          // fixme: temporary, simple text cleanup
//          val text = unfiltered.replace("\n",
//            " ").replace("- ", "")
//          val filename = StringUtils.afterLast(file.getName, '/')
//          println(s"going to parse input file: $filename")
//          val (doc, mentions) = vp.parse(text)
//          val printVars = PrintVariables("Belief", "believer", "belief")
//          val context = mutable.Map.empty[Int, ContextDetails]
//
//          multiPrinter.outputMentions(mentions, doc, context, filename, printVars)
//        }
//        catch {
//          case e: Exception => e.printStackTrace()
//        }
//      }
//    }

}