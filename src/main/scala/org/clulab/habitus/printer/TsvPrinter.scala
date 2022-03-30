package org.clulab.habitus.printer

import org.clulab.habitus.utils.Context
import org.clulab.odin.Mention
import org.clulab.processors.Document

class TsvPrinter(outputFilename: String) extends Printer(outputFilename) {

  protected def outputMention(mention: Mention, doc: Document, inputFilename: String, printVariables: PrintVariables): Unit = {
    val sentenceText = mention.sentenceObj.getSentenceText
    val argumentInfo = ArgumentInfo(mention, printVariables)
    val contextString = mention.attachments.headOption.map(_.asInstanceOf[Context].getTSVContextString).getOrElse("")

    printWriter.print(s"${argumentInfo.variableText}\t${argumentInfo.valueText}\t${argumentInfo.valueNorm}\t$sentenceText\t$inputFilename\t")
    printWriter.println(contextString)
  }
}
