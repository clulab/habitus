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
    val doc = super.mkDocument(text, keepText)
    removeBadSentences(doc)
  }

  def mkDocumentWithRestoreCase(text: String, keepText: Boolean = false): Document = {
    val doc = super.mkDocument(text, keepText)
    removeBadSentences(doc)
    restoreCase(doc)
    doc
  }

  private def removeBadSentences(doc: Document): Document = {
    val cleanSents = doc.sentences.filterNot(isBadSentence)
    val cleanDoc = new Document(cleanSents)
    cleanDoc.id = doc.id
    cleanDoc.text = doc.text
    cleanDoc
  }

  private def getAlphaCount(sentence: Sentence): Int = {
    sentence.words.count(_.exists(_.isLetter))
  }

  /** Returns true if this is a malformed sentence
    * malformed= either > 150 tokens or
    * more than 50% of tokens are numbers */
  private def isBadSentence(sentence: Sentence): Boolean = {
    val isBad = sentence.words.length > 150 || getAlphaCount(sentence) < sentence.words.length / 2
    // println(s"isBad = $isBad")
    isBad
  }
}
