package org.clulab.habitus.apps.utils

object WordTypes extends Enumeration {
  type WordType = Value
  val AllLower, AllUpper, InitialUpper, NonWord, Other = Value

  def apply(word: String): WordType = {
    require(word.nonEmpty)

    val letterCount = word.count(_.isLetter)
    // isLower and isUpper on non-letters will both be false.
    val lowerCount = word.count(_.isLower)
    val upperCount = word.count(_.isUpper)

    if (letterCount == 0)
      NonWord
    else if (lowerCount == letterCount)
      AllLower
    else if (upperCount == letterCount)
      AllUpper
    else if (upperCount == 1 && word.head.isUpper)
      InitialUpper
    else Other
  }
}
