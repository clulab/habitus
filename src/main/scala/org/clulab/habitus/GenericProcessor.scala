package org.clulab.habitus

import org.clulab.habitus.document.attachments.YearDocumentAttachment
import org.clulab.habitus.variables.EntityDistFreq
import org.clulab.odin.Mention
import org.clulab.processors.Document

case class ParsingResult(document: Document, allMentions: Seq[Mention], targetMentions: Seq[Mention])

trait GenericProcessor {
  def parse(text: String, yearOpt: Option[Int] = None): ParsingResult

  def setYear(doc: Document, yearOpt: Option[Int]): Unit = {
    yearOpt.foreach { year =>
      YearDocumentAttachment.setYear(doc, year)
    }
  }
}
