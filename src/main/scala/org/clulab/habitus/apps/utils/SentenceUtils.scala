package org.clulab.habitus.apps.utils

import org.clulab.processors.Sentence

object SentenceUtils {

  def getText(sentence: Sentence): String = {
    val separatedWords = sentence.indices.map { i =>
      val separator = {
        val spaceCount =
            if (i == 0) 0
            else sentence.startOffsets(i) - sentence.endOffsets(i - 1)
        " " * spaceCount
      }

      separator + sentence.words(i)
    }

    separatedWords.mkString("")
  }
}
