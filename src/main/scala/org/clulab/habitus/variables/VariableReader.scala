package org.clulab.habitus.variables

import org.clulab.habitus.utils.{ContextExtractor, DefaultContextExtractor, JsonlPrinter, Lazy, MultiPrinter, PrintVariables, TsvPrinter}
import org.clulab.utils.FileUtils
import org.clulab.utils.StringUtils
import org.clulab.utils.ThreadUtils
import org.clulab.utils.Closer.AutoCloser

import java.io.File

object VariableReader {

  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToMap(args)
    val inputDir = props("in")
    val outputDir = props("out")
    val masterResource = props.getOrElse("grammar", "/variables/master.yml")
    val threads = props.get("threads").map(_.toInt).getOrElse(1)

    run(inputDir, outputDir, threads, masterResource)
  }

  def run(inputDir: String, outputDir: String, threads: Int, masterResource: String): Unit = {
    new File(outputDir).mkdir()

    def mkOutputFile(extension: String): String = outputDir + "/mentions" + extension

    val vp = VariableProcessor()
    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files

    new MultiPrinter(
      Lazy{new TsvPrinter(mkOutputFile(".tsv"))},
      Lazy(new JsonlPrinter(mkOutputFile(".jsonl")))
    ).autoClose { multiPrinter =>
      for (file <- parFiles) {
        try {
          val text = FileUtils.getTextFromFile(file)
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          val (doc, mentions, allEventMentions, entityHistogram) = vp.parse(text)


          val printVars = PrintVariables("Assignment", "variable", "value")
          multiPrinter.outputMentions(allEventMentions, doc, filename, printVars)
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
  }

}
