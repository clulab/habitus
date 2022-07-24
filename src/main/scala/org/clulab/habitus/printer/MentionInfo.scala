package org.clulab.habitus.printer

import org.clulab.habitus.utils.Pairable
import org.clulab.odin.Mention

case class MentionInfo(contextWindow: String, sentenceText: String, inputFilename: String, label: String) extends Pairable

object MentionInfo {

  def apply(mention: Mention, inputFilename: String): MentionInfo = {
    val windowSize = 3
    val document = mention.document
    val sentInterval = (math.max(mention.sentence - windowSize, 0), math.min(mention.sentence + windowSize, mention.document.sentences.length + 1))
    val contextWindow = mention.document.sentences.slice(sentInterval._1, sentInterval._2).map(_.getSentenceText).mkString(" ")
    val sentenceText = document.sentences(mention.sentence).getSentenceText
    val label = mention.label
    new MentionInfo(contextWindow, sentenceText, inputFilename, label)
  }
}


