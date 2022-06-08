package org.clulab.habitus.variables
import org.clulab.utils.Closer.AutoCloser
import org.clulab.odin.TextBoundMention
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}

import java.io.{File, PrintWriter}

object CropAnalysis {

  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToMap(args)
    val inputDir = ""
    val outputDir = ""
    val threads = props.get("threads").map(_.toInt).getOrElse(1)

    run(inputDir, outputDir, threads)
  }

  def run(inputDir: String, outputDir: String, threads: Int): Unit = {
    new File(outputDir).mkdirs()

    // fixme: temporary, simple text cleanup
    def cleanText(text: String): String = text
      .replace("\n", " ")
      .replace("- ", "")

    val processor = VariableProcessor()
    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files

    new PrintWriter(new File(outputDir + "/mentions.tsv")).autoClose { pw =>
      pw.println("filename\tmention type\tfound by\tsentence\tmention text\targs in all next columns (argType: argText)")
      for (file <- parFiles) {
        try {
          val unfiltered = FileUtils.getTextFromFile(file)
          val text = cleanText(unfiltered)
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          val parsingResults = processor.parse(text)
          val targetMentions = parsingResults.allMentions
          val contentMentions = targetMentions //.filter(m => m.label == "CropAssignment" || m.label == "Crop")
          for (m <- contentMentions) {
            pw.print(s"${filename}\t${m.label}\t${m.foundBy}\t${m.sentenceObj.getSentenceText}\t${m.text}")
            if (!m.isInstanceOf[TextBoundMention]) {
              for ((key, values) <- m.arguments) {
                if (values.nonEmpty) {
                  // multiple args of same type are "::"-separated
                  val value = values.map(_.text.trim().replace("\t", "")).mkString("::")
                  pw.print(s"\t$key:\t$value")
                }
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