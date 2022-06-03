package org.clulab.habitus.printer

import org.clulab.habitus.utils.Pairable
import org.clulab.odin.Mention

case class MentionInfo(sentenceText: String, inputFilename: String, label: String) extends Pairable

object MentionInfo {

  def apply(mention: Mention, inputFilename: String): MentionInfo = {
    val document = mention.document
    val sentenceText = document.sentences(mention.sentence).getSentenceText
    val label = mention.label
    new MentionInfo(sentenceText, inputFilename, label)
  }
}


