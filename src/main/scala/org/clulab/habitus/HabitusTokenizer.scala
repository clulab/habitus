package org.clulab.habitus

import org.clulab.processors.Sentence
import org.clulab.processors.clu.tokenizer.Tokenizer

import scala.util.matching.Regex

class HabitusTokenizer(tokenizer: Tokenizer) extends Tokenizer(tokenizer.lexer, tokenizer.steps, tokenizer.sentenceSplitter) {
  // TODO: Make sure en dash is preserved in raw somehow!

  override def tokenize(text: String, sentenceSplit: Boolean = true): Array[Sentence] = {
    // Cheat and swap out some en dashes if necessary.
    val habitusText =
      if (text.contains(HabitusTokenizer.endash))
        HabitusTokenizer.regex.replaceAllIn(text,HabitusTokenizer.replacer)
      else
        text

    val sentences = tokenizer.tokenize(habitusText, sentenceSplit)

    sentences
  }
}

object HabitusTokenizer {
  val dash = "-"
  val endash = "\u2013"
  val regex = {
    val start = "^"
    val end = "$"
    val notDigit = "[^\\d]"
    val exponent = "[12]" // So far 1 and are all we've encountered as exponents.

    s"($start|$notDigit)$endash($exponent($notDigit|$end))".r
  }
  val replacer: Regex.Match => String = m => s"${m.group(1)}$dash${m.group(2)}"
}
