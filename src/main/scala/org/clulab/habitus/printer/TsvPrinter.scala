package org.clulab.habitus.printer

import org.clulab.habitus.utils.Context
import org.clulab.wm.eidoscommon.utils.TsvWriter

import java.io.File
import scala.collection.mutable

class TsvPrinter(outputFile: File) extends Printer(outputFile) {

  def this(outputFilename: String) = this (new File(outputFilename))

  protected var clean = true
  protected var tsvWriter = new TsvWriter(printWriter)
  protected val nameToColumnMap: mutable.Map[String, Int] = mutable.Map.empty

  protected def outputHeaders(mentionInfo: MentionInfo, contextInfo: Context): Unit = {
    val mentionNames = mentionInfo.getNames
    val contextNames = contextInfo.getNames
    val argumentNames = Seq("arg0_name", "arg0_text", "arg0_norm", "...")

    tsvWriter.println(mentionNames ++ contextNames ++ argumentNames)
  }

  def outputInfos(
     mentionInfo: MentionInfo,
     contextInfo: Context,
     argumentInfos: Seq[ArgumentInfo]
  ): Unit = {
    if (clean) {
      clean = false
      outputHeaders(mentionInfo, contextInfo)
    }
    val mentionValues = mentionInfo.getValues
    val contextValues = contextInfo.getValues
    val columnToValuesMap = argumentInfos.map { argumentInfo =>
      val column = nameToColumnMap.getOrElseUpdate(argumentInfo.name, nameToColumnMap.size)
      val values = argumentInfo.getValues.map(_.toString)

      column -> values
    }.toMap.withDefaultValue(TsvPrinter.emptyColumnInfo)
    val maxColumn = if (columnToValuesMap.nonEmpty) columnToValuesMap.keys.max else -1
    val argumentValues = Range.inclusive(0, maxColumn).flatMap { column =>
      columnToValuesMap(column)
    }

    tsvWriter.println(mentionValues ++ contextValues ++ argumentValues)
  }
}

object TsvPrinter {
  val emptyColumnInfo: Seq[String] = Array.fill(ArgumentInfo.width)("")
}
