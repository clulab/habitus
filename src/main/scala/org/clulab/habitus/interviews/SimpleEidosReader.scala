package org.clulab.habitus.interviews

import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}
import org.clulab.wm.eidos.SimpleEidos
import org.clulab.wm.eidos.serialization.simple.SimpleSerializer

import java.io.{File, PrintWriter}

object SimpleEidosInterviewsReader {

  def main(args: Array[String]): Unit = {
    val inputDir = "/home/alexeeva/Desktop/habitus_related/aabc_data/influence-rules-meet-eidos"
    val outputDir = "/home/alexeeva/Desktop/habitus_related/aabc_data/influence-rules-meet-eidos/output"
    val threads = 1

    example()
//    run(inputDir, outputDir, threads)
  }

  def example(): Unit = {
    val text = "This is a test.  Water trucking in Ethiopia has decreased over August due to the cost of fuel."
    val eidosSystem = SimpleEidos(useGeoNorm = false, useTimeNorm = false)
    val annotatedDocument = eidosSystem.extractFromText(text)
    val simpleSerializer = SimpleSerializer(annotatedDocument)

    simpleSerializer.serialize()
  }

  def run(inputDir: String, outputDir: String, threads: Int): Unit = {
    new File(outputDir).mkdir()

    val vp = InterviewsProcessor()
    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files

    new PrintWriter(new File(outputDir + "/mentions.tsv")).autoClose { pw =>
      pw.println("filename\tmention type\tfound by\tsentence\tmention text\targs in all next columns (argType: argText)")
      for (file <- parFiles) {
        try {
          val unfiltered = FileUtils.getTextFromFile(file)
          // fixme: temporary, simple text cleanup
          val text = unfiltered.replace("\n",
            " ").replace("- ", "")
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          val result = vp.parse(text)
          val mentions = result.targetMentions
          val contentMentions = mentions.filter(m => m.labels.contains("Event"))
          for (m <- contentMentions) println("Men: " + m.label + " " + m.text)

          for (m <- contentMentions) {
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
      }
    }
  }
}

