package org.clulab.habitus.printer

import org.clulab.habitus.utils.Context
import org.clulab.odin.Mention
import org.clulab.utils.{FileUtils, Logging, Sink, Sinker, Sourcer}

import java.io.{BufferedOutputStream, File, FileOutputStream, OutputStreamWriter, PrintWriter}
import java.nio.charset.{Charset, StandardCharsets}

abstract class Printer(outputFile: File) extends Printing {

  def this(outputFilename: String) = this(new File(outputFilename))

  protected val printWriter: PrintWriter = Printer.newPrintWriter(outputFile)
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

  def outputMentions(mentions: Seq[Mention], inputFilename: String): Unit = {
    println(s"Writing mentions from doc $inputFilename to ${outputFile.getName}")
    mentions
        .filter { mention => mention.attachments.nonEmpty && mention.attachments.head.isInstanceOf[Context] }
        .sortBy(toTuple)
        .foreach { mention =>
          val mentionInfo = MentionInfo(mention, inputFilename)
          val contextInfo = mention.attachments.head.asInstanceOf[Context]
          val argumentKeys = getArgumentKeys(mention)
          val argumentInfos = argumentKeys.map { argumentKey =>
            val mentions = mention.arguments(argumentKey)
            val length = mentions.length

            if (length != 1)
              println(s"Warning: There are $length $argumentKey arguments when exactly 1 is expected.  Only the first will be printed!")
            ArgumentInfo(argumentKey, mentions.head)
          }
          outputInfos(mentionInfo, contextInfo, argumentInfos)
        }
    printWriter.flush()
  }
}

object Printer extends Logging {

  def newPrintWriter(file: File, append: Boolean = false): PrintWriter = {
    logger.info("Sinking file " + file.getPath)

    val outputStreamWriter = new OutputStreamWriter(
      new BufferedOutputStream(new FileOutputStream(file, append)),
      StandardCharsets.UTF_8
    )

    // We're enforcing LF even for Windows so that outputs will compare exactly.
    // See https://stackoverflow.com/questions/1014287/is-there-a-way-to-make-printwriter-output-to-unix-format
    new PrintWriter(outputStreamWriter) {
      override def println(): Unit = write('\n')
    }
  }
}
