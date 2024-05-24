package org.clulab.habitus.printer

import org.clulab.habitus.utils.Context
import org.clulab.habitus.utils.TsvWriter

import java.io.File
import scala.collection.mutable

class StaticArgsTsvPrinter(outputFile: File) extends Printer(outputFile) {

  def this(outputFilename: String) = this (new File(outputFilename))
  protected var argsOpt: Option[Seq[String]] = None
  protected val tsvWriter = new TsvWriter(printWriter)
  protected val nameToColumnMap: mutable.Map[String, Int] = mutable.Map.empty

  protected def outputHeaders(mentionInfo: MentionInfo, contextInfo: Context, argumentInfos: Seq[ArgumentInfo]): Unit = {
    val mentionNames = mentionInfo.getNames
    val contextNames = contextInfo.getNames
    val argumentNames = argumentInfos.flatMap { argumentInfo =>
      Seq(s"${argumentInfo.name}_text", s"${argumentInfo.name}_norm")
    }
    tsvWriter.println(mentionNames ++ contextNames ++ argumentNames)
  }

  def outputInfos(
     mentionInfo: MentionInfo,
     contextInfo: Context,
     argumentInfos: Seq[ArgumentInfo]
  ): Unit = {
    if (argsOpt.isEmpty) {
      argsOpt = Some(argumentInfos.map(_.name))
      outputHeaders(mentionInfo, contextInfo, argumentInfos)
    }
    else
      require(argsOpt.get == argumentInfos.map(_.name))

    val mentionValues = mentionInfo.getValues
    val contextValues = contextInfo.getValues
    val columnToValuesMap = argumentInfos.map { argumentInfo =>
      val column = nameToColumnMap.getOrElseUpdate(argumentInfo.name, nameToColumnMap.size)
      val values = argumentInfo.getValues.drop(1).map(_.toString) // Skip the name.

      column -> values
    }.toMap.withDefaultValue(StaticArgsTsvPrinter.emptyColumnInfo)
    val maxColumn = if (columnToValuesMap.nonEmpty) columnToValuesMap.keys.max else -1
    val argumentValues = Range.inclusive(0, maxColumn).flatMap { column =>
      columnToValuesMap(column)
    }

    tsvWriter.println(mentionValues ++ contextValues ++ argumentValues)
  }
}

object StaticArgsTsvPrinter {
  val emptyColumnInfo: Seq[String] = Array.fill(ArgumentInfo.width)("")
}
