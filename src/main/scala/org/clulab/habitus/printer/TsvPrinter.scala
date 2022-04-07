package org.clulab.habitus.printer

import org.clulab.habitus.utils.Context
import org.clulab.wm.eidoscommon.utils.TsvWriter

import java.io.File

class TsvPrinter(outputFile: File) extends Printer(outputFile) {

  def this(outputFilename: String) = this (new File(outputFilename))

  protected var clean = true
  protected var tsvWriter = new TsvWriter(printWriter)

  // keep track of order of arguments

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
    // TODO: Keep these in the right order, fill in empties
    // TODO: Maybe skip None or Some
    val argumentValues = argumentInfos.flatMap { argumentInfo =>
      val pairs = argumentInfo.getPairs

      pairs.map(_._2)
    }

    tsvWriter.println(mentionValues ++ contextValues ++ argumentValues)
  }
}
