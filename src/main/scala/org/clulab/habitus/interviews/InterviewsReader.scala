package org.clulab.habitus.interviews

import org.clulab.odin.{EventMention, TextBoundMention}
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}

import java.io.{File, PrintWriter}

object InterviewsReader {

  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToMap(args)
    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/test-habitus-main/input"
    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/test-habitus-main/output"
    val threads = 1//props.get("threads").map(_.toInt).getOrElse(1)

    run(inputDir, outputDir, threads)
  }

  def run(inputDir: String, outputDir: String, threads: Int): Unit = {
    new File(outputDir).mkdir()

    val interviewReader = InterviewsProcessor()

    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files

    new PrintWriter(new File(outputDir + "/mentions.tsv")).autoClose { pw =>
      pw.println("filename\tmention type\tfound by\tsentence\tmention text\trelation_direction\targs in all next columns (argType: argText)")
      for (file <- parFiles) {
        try {
          val unfiltered = FileUtils.getTextFromFile(file)
          // fixme: temporary, simple text cleanup
          val text = unfiltered.replace("\n",
            " ").replace("- ", "")
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          val parsingResults = interviewReader.parse(text)
          val targetMentions = parsingResults.targetMentions
          val contentMentions = targetMentions.filter(m => m.labels.contains("Event") & !m.isInstanceOf[TextBoundMention])
          for (m <- contentMentions) {
            val direction = if (m.isInstanceOf[EventMention]) {
              val trigger = m.asInstanceOf[EventMention].trigger.text
              if (trigger.contains("increas")) "increase"
              else if (trigger.contains("decreas")) "decrease"
              else "N/A"
            } else "N/A"
            pw.print(s"${filename}\t${m.label}\t${m.foundBy}\t${m.sentenceObj.getSentenceText}\t${m.text}\t${direction}")
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
      }
    }
  }
}
