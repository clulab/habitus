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

  private def getAlphaCount(sentence: Sentence): Int = {
    sentence.words.count(_.exists(_.isLetter))
  }

  def mostSingleLettersInARow(sentence: Sentence): Int = {
    // This could be sentence.words instead of raw.
    val (count, max) = sentence.raw.foldLeft((0, 0)) { case ((count, max), raw) =>
      if (raw.length == 1) (count + 1, math.max(count + 1, max))
      else (0, max)
    }
    max
  }

  /** Returns true if this is a malformed sentence
    * malformed= either > 150 tokens or
    * more than 50% of tokens are numbers */
  def isBadSentence(sentence: Sentence): Boolean = {
    val isBad = {
        !HabitusProcessor.wordCountRange.contains(sentence.words.length) ||
        getAlphaCount(sentence) < sentence.words.length / 2 ||
        HabitusProcessor.singleLetterLimit < mostSingleLettersInARow(sentence)
    }
    // println(s"isBad = $isBad")
    isBad
  }
}

object HabitusProcessor {
  val wordCountRange = Range.inclusive(3, 150)
  val singleLetterLimit = 15
}