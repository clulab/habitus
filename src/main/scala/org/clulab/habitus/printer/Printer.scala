package org.clulab.habitus.printer

import org.clulab.habitus.utils.Context
import org.clulab.odin.Mention
import org.clulab.utils.FileUtils

import java.io.{File, PrintWriter}

abstract class Printer(outputFile: File) extends Printing {

  def this(outputFilename: String) = this(new File(outputFilename))

  protected val printWriter: PrintWriter = FileUtils.printWriterFromFile(outputFile)
  val toTuple: Mention => (Int, Int, Int) =
      (mention: Mention) => (mention.sentence, mention.start, mention.end)
  val na = "N/A"

  def outputInfos(
    mentionInfo: MentionInfo,
    contextInfo: Context,
    argumentInfos: Seq[ArgumentInfo]
  ): Unit

  def close(): Unit = {
    printWriter.close()
  }

  def outputMentions(
    mentions: Seq[Mention],
    inputFilename: String
  ): Unit = {
    println(s"Writing mentions from doc $inputFilename to ${outputFile.getName}")
    mentions
        .filter { mention => mention.attachments.nonEmpty && mention.attachments.head.isInstanceOf[Context] }
        .sortBy(toTuple)
        .foreach { mention =>
          val mentionInfo = MentionInfo(mention, inputFilename)
          val contextInfo = mention.attachments.head.asInstanceOf[Context]
          val argumentKeys = mention.arguments.keys.toSeq.sorted
          val argumentInfos = argumentKeys.map { argumentKey =>
            val mentions = mention.arguments(argumentKey)
            val length = mentions.length

            assert(length > 0) // The arguments shouldn't even exist otherwise.
            if (length != 1)
              println(s"Warning: There are $length $argumentKey arguments when exactly 1 is expected.  Only the first will be printed!")
            ArgumentInfo(argumentKey, mentions.head)
          }
          outputInfos(mentionInfo, contextInfo, argumentInfos)
        }
    printWriter.flush()
  }
}
