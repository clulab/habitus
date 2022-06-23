package org.clulab.habitus


import org.clulab.processors.{Document, Sentence}
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.clu.tokenizer.Tokenizer
import org.clulab.sequences.LexiconNER

class HabitusProcessor(lexiconNer: Option[LexiconNER]) extends CluProcessor(optionalNER = lexiconNer) {
  /** Our own tokenizer to clean up some nasty characters */
  lazy val habitusTokenizer: HabitusTokenizer = new HabitusTokenizer(localTokenizer)
  override lazy val tokenizer: Tokenizer = habitusTokenizer

  /** Our own mkDocument, which removes some malformed sentences */
  override def mkDocument(text: String, keepText: Boolean = false): Document = {
    val oldDoc = super.mkDocument(text, keepText)
    val newDoc = removeBadSentences(oldDoc)
    newDoc
  }

  def mkDocumentWithRestoreCase(text: String, keepText: Boolean = false): Document = {
    val oldDoc = super.mkDocument(text, keepText)
    val newDoc = removeBadSentences(oldDoc)
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

  /** Returns true if this is a malformed sentence
    * malformed= either > 150 tokens or
    * more than 50% of tokens are numbers */
  def isBadSentence(sentence: Sentence): Boolean = {
    val isBad =
        !HabitusProcessor.wordCountRange.contains(sentence.words.length) ||
        HabitusProcessor.nonAlphaLimit < getNonAlphaCount(sentence).toDouble / sentence.words.length ||
        HabitusProcessor.singleLetterLimit < mostSingleLettersInARow(sentence) ||
        HabitusProcessor.customKernLimit < mostCustomKernsInARow(sentence)
    // println(s"isBad = $isBad")
    isBad
  }
}

object HabitusProcessor {
  val wordCountRange = Range.inclusive(3, 150)
  val nonAlphaLimit = 0.45
  val singleLetterLimit = 15
  val customKernLimit = 15
}