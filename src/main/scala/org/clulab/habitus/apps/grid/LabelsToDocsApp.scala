package org.clulab.habitus.apps.grid

import org.clulab.utils.{FileUtils, Sourcer}
import org.clulab.wm.eidoscommon.utils.TsvReader

import scala.collection.mutable.ArrayBuffer
import scala.util.Using

object LabelsToDocsApp extends App {
  val inputFileName = args.lift(0).getOrElse("../corpora/senegal/experiment/row_labels_harvest.tsv")
  val outputDirName = args.lift(1).getOrElse("../corpora/senegal/experiment/articles")

  val rows: Map[String, ArrayBuffer[String]] = Map(
    "conditions" -> new ArrayBuffer[String](),
    "decisions" -> new ArrayBuffer[String](),
    "processes" -> new ArrayBuffer[String](),
    "proportions" -> new ArrayBuffer[String](),
    "causes" -> new ArrayBuffer[String](),
    "other" -> new ArrayBuffer[String]()
  )

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { source =>
    val tsvReader = new TsvReader()
    val lines = source.getLines().drop(1)

    lines.foreach { line =>
      val Array(_, _, readable, conditions, decisions, processes, proportions, causes, other, _) = tsvReader.readln(line)
      val bitmap = s"$conditions$decisions$processes$proportions$causes$other"
      val printable = {
        val trimmed = readable.trim
        val unquoted =
            if (trimmed.head == '"' && trimmed.last == '"') trimmed.drop(1).dropRight(1)
            else trimmed
        val retrimmed = unquoted.trim

        retrimmed
      }


      assert(bitmap.count(_ == '1') >= 1)

      if (conditions == "1")  rows("conditions").append(printable)
      if (decisions == "1")   rows("decisions").append(printable)
      if (processes == "1")   rows("processes").append(printable)
      if (proportions == "1") rows("proportions").append(printable)
      if (causes == "1")      rows("causes").append(printable)
      if (other == "1")       rows("other").append(printable)
    }
  }

  rows.foreach { case (name, sentences) =>
    Using.resource(FileUtils.printWriterFromFile(s"$outputDirName/$name.txt")) { printWriter =>

      sentences.foreach { sentence => printWriter.println(sentence.trim) }
    }
  }
}
