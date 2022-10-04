package org.clulab.habitus


import org.clulab.processors.{Document, Sentence}
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.clu.tokenizer.Tokenizer
import org.clulab.sequences.LexiconNER

class HabitusProcessor(lexiconNer: Option[LexiconNER], filter: Boolean = true) extends CluProcessor(optionalNER = lexiconNer) {
  /** Our own tokenizer to clean up some nasty characters */
  lazy val habitusTokenizer: HabitusTokenizer = new HabitusTokenizer(lazyTokenizer.value)
  override lazy val tokenizer: Tokenizer = habitusTokenizer

  /** Our own mkDocument, which removes some malformed sentences */
  override def mkDocument(text: String, keepText: Boolean = false): Document = {
    val cleanedupText = text.replace("e.g. ", "e.g., ") // fixme: this needs to be handled elsewhere, but this issue results in bad sentence tokenization
    val oldDoc = super.mkDocument(cleanedupText, keepText)
    val newDoc = if (filter) removeBadSentences(oldDoc) else oldDoc
    newDoc
  }

  def mkDocumentWithRestoreCase(text: String, keepText: Boolean = true): Document = {
    val oldDoc = super.mkDocument(text, keepText)
    val newDoc = if (filter) removeBadSentences(oldDoc) else oldDoc
    restoreCase(newDoc)
    newDoc
  }

  def copyDoc(oldDoc: Document, newSentences: Array[Sentence]): Document = {
    val newDoc = new Document(newSentences)

    newDoc.id = oldDoc.id
    newDoc.text = oldDoc.text
    newDoc
  }

  private def removeBadSentences(doc: Document): Document = {
    val cleanSents = doc.sentences.filterNot(isBadSentence)

    copyDoc(doc, cleanSents)
  }

  private def getAlphaCount(sentence: Sentence): Int =
      sentence.words.count(_.exists(_.isLetter))

  private def getNonAlphaCount(sentence: Sentence): Int =
      sentence.words.length - getAlphaCount(sentence)

  def mostInARow(strings: Seq[String], predicate: String => Boolean): Int = {
    val (count, max) = strings.foldLeft((0, 0)) { case ((count, max), string) =>
      if (predicate(string)) (count + 1, math.max(count + 1, max))
      else (0, max)
    }

    max
  }

  def mostSingleLettersInARow(sentence: Sentence): Int = {
    // This could be sentence.words instead of raw.
    mostInARow(sentence.raw, string => string.length == 1)
  }

  def mostCustomKernsInARow(sentence: Sentence): Int = {
    // This could be sentence.words instead of raw.
    // These are usually single or double letters.  Sometimes there are three
    // from a ligature and there could be more, but they have not been encountered
    // and they start looking more like real words.
    mostInARow(sentence.raw, string => 1 <= string.length && string.length <= 3)
  }

  def mostFloatsInARow(sentence: Sentence): Int = {
    // This could be sentence.words instead of raw.
    // Make sure there is at least one digit, even leading 0s, and then only
    // more digits or decimal points, even if there are multiple decimal points.
    // Integers are a subset of floats.  The decimal point isn't required.
    mostInARow(sentence.raw, string => {
      string.exists("123456789".contains(_)) &&
      string.forall("0123456789.".contains(_))
    })
  }

  def capitalizedWordPercentage(sentence: Sentence): Float = {
    // Counts percentage of words in a sentence that are capitalized
    sentence.words.count(_.head.isUpper).toFloat / sentence.words.length
  }

  /** Returns true if this is a malformed sentence
    * malformed= either > 150 tokens or
    * more than 50% of tokens are numbers */
  def isBadSentence(sentence: Sentence): Boolean = {
    val length = sentence.words.length
    val isBad =
        !HabitusProcessor.wordCountRange.contains(length) ||
        HabitusProcessor.nonAlphaCountLimit(length) < getNonAlphaCount(sentence) ||
        HabitusProcessor.singleLetterLimit < mostSingleLettersInARow(sentence) ||
        HabitusProcessor.customKernLimit < mostCustomKernsInARow(sentence) ||
        HabitusProcessor.floatLimit < mostFloatsInARow(sentence) ||
        HabitusProcessor.capitalizedWordLimit < capitalizedWordPercentage(sentence)

    // println(s"isBad = $isBad")
    isBad
  }
}

object HabitusProcessor {
  val wordCountRange = Range.inclusive(3, 110)
  val nonAlphaLimit = 0.50
  val singleLetterLimit = 15
  val customKernLimit = 15
  val floatLimit = 5
  val capitalizedWordLimit = 0.50 // allow up to 50% of words in a sentence to be capitalized (trying to eliminate headlines)

  def nonAlphaCountLimit(length: Int): Int = {
    // The integer arithmetic used here originally made use of truncation.
    // For positive values, that's the same as floor.
    // The condition should be specifiable as limit < function(sentence)
    // with limit a float/double so it has been rewritten.
    // getAlphaCount(sentence) < sentence.words.length / 2 ||
    // getAlphaCount(sentence) < math.floor(sentence.words.length * alphaLimit) ||
    // math.ceil(sentence.words.length * nonAlphaLimit) < getNonAlphaCount(sentence)
    math.ceil(length * nonAlphaLimit).toInt
  }
}