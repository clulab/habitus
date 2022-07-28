package org.clulab.habitus.printer

import com.typesafe.config.ConfigFactory
import ai.lum.common.ConfigUtils._
import org.clulab.habitus.utils.Pairable
import org.clulab.odin.Mention

case class MentionInfo(contextWindow: String, sentenceText: String, inputFilename: String, label: String) extends Pairable

object MentionInfo {

  val config = ConfigFactory.load()
  val windowSize: Int = config[Int]("VarDatesReader.contextWindowSize")

  def apply(mention: Mention, inputFilename: String): MentionInfo = {
    val document = mention.document
    val sentInterval = (math.max(mention.sentence - windowSize, 0), math.min(mention.sentence + windowSize + 1, mention.document.sentences.length))
    val contextWindow = mention.document.sentences.slice(sentInterval._1, sentInterval._2).map(_.getSentenceText).mkString(" ")
    val sentenceText = document.sentences(mention.sentence).getSentenceText
    val label = mention.label
    new MentionInfo(contextWindow, sentenceText, inputFilename, label)
  }
}


