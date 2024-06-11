package org.clulab.habitus.apps.grid

import org.clulab.utils.{FileUtils, Sourcer, StringUtils}

import scala.util.Using

object SplitDatasetsApp extends App {

  class FileNameFormatter(baseFileName: String) {
    val left = StringUtils.beforeLast(baseFileName, '.')
    val right = StringUtils.afterLast(baseFileName, '.')

    def format(index: Int): String = {
      s"$left-$index.$right"
    }
  }

  val inputFileName = args.lift(0).getOrElse("/home/kwa/data/Corpora/habitus-project/corpora/ghana-sitemap/articlesandeidos/www_ghanaweb_com.tsv")
  val count = args.lift(1).getOrElse("3000000").toInt
  val outputFileNameFormatter = new FileNameFormatter(inputFileName)

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { source =>
    val lines = source.getLines
    val header = lines.next
    var bufferedLine: Option[String] = None
    var index = 0

    while (lines.hasNext) {
      val batchOfLines = lines.take(count).toArray
      val outputFileName = outputFileNameFormatter.format(index)

      index += 1
      Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
        printWriter.println(header)
        bufferedLine.foreach(printWriter.println)
        bufferedLine = None
        batchOfLines.foreach(printWriter.println)

        if (lines.hasNext) {
          val prevLine = lines.next
          printWriter.println(prevLine)
          val prevUrl = StringUtils.beforeFirst(prevLine, '\t')

          while (lines.hasNext && {
            val nextLine = lines.next
            val nextUrl = StringUtils.beforeFirst(nextLine, '\t')

            if (prevUrl == nextUrl) {
              printWriter.println(nextLine)
              true
            }
            else {
              bufferedLine = Some(nextLine)
              false
            }
          }) {}
        }
      }
    }
  }
}
