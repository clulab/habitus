package org.clulab.habitus

import org.clulab.odin.{EventMention, Mention}
import org.clulab.processors.{Document, Sentence}
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.clu.tokenizer.Tokenizer
import org.clulab.sequences.LexiconNER

import scala.collection.mutable.ArrayBuffer
import scala.util.Try
import scala.util.control.Breaks.{break, breakable}

class HabitusProcessor(lexiconNer: Option[LexiconNER]) extends CluProcessor(optionalNER = lexiconNer) {
  /** Our own tokenizer to clean up some nasty characters */
  lazy val habitusTokenizer: HabitusTokenizer = new HabitusTokenizer(localTokenizer)
  override lazy val tokenizer: Tokenizer = habitusTokenizer

  /** Our own mkDocument, which removes some malformed sentences */
  override def mkDocument(text:String, keepText:Boolean = false): Document = {
    val doc = super.mkDocument(text, keepText)
    removeBadSentences(doc)
  }

  private def removeBadSentences(doc: Document): Document = {
    val cleanSents = doc.sentences.filterNot(isBadSentence)
    val cleanDoc = new Document(cleanSents)
    cleanDoc.id = doc.id
    cleanDoc.text = doc.text
    cleanDoc
  }

  /** Returns true if this is a malformed sentence */
  private def isBadSentence(sentence: Sentence): Boolean = {
    // keep only beliefs that have less than 150 tokens and <50% of tokens being numbers
    val shortBeliefMentions = ""
    var numbersCounter = 0
    for (word <- sentence.words) {
      breakable {
        for (char <- word) {
          if (char.isDigit) {
            numbersCounter += 1
            break
          }
        }
      }
    }
    numbersCounter > (sentence.words.length/2)
  }


  //keep only beliefs Sentences with <50% of tokens being numbers
  private def halfOfTokensAreNumbers(m: Mention): Boolean = {
    m.isInstanceOf[EventMention] &&
      m.arguments.contains("belief") &&
      m.arguments("belief").nonEmpty &&
      checkNumberCount(m)
  }

  private def checkNumberCount(mention: Mention): Boolean = {
    var numberCounter = 0
    for (word <- mention.sentenceObj.words) {
      if (Try(word.toInt).isSuccess || Try(word.toFloat).isSuccess || Try(word.toDouble).isSuccess || Try(word.toLong).isSuccess ) {
        numberCounter = numberCounter + 1
      }
    }
    numberCounter < mention.sentenceObj.words.length / 2
  }

  //keep only beliefs which have less than 150 tokens
  private def containsLessThan150Tokens(m: Mention): Boolean = {
    m.isInstanceOf[EventMention] &&
      m.arguments.contains("belief") &&
      m.arguments("belief").nonEmpty &&
      (m.sentenceObj.words.length < 150)
  }
}
