package org.clulab.habitus.apps

import org.clulab.embeddings.{WordEmbeddingMap, WordEmbeddingMapPool}
import org.clulab.scala_transformers.tokenizer.Tokenizer
import org.clulab.scala_transformers.tokenizer.jni.ScalaJniTokenizer

// In this very simple grounder, a Seq of words is being matched with the
// single word in the embedding map keys that most closely summarizes or
// approximates them.  Perhaps "male ruler" results in "king".
class HabitusGrounder(wordEmbeddingMap: WordEmbeddingMap, tokenizer: Tokenizer, specialCharOpt: Option[Char]) {

  def ground(words: Array[String]): String = {
    val tokens = tokenizer.tokenize(words).tokens
    val middleTokens = tokens.drop(1).dropRight(1) // Skip the [CLS] and [SEP].
    val compositeVector = wordEmbeddingMap.makeCompositeVector(middleTokens)
    val grounding = wordEmbeddingMap.keys.maxBy { key =>
      val vector = wordEmbeddingMap.get(key).get
      val similarity = WordEmbeddingMap.dotProduct(compositeVector, vector)

//      println(s"$key\t$similarity")
      similarity
    }

    if (specialCharOpt.isEmpty || specialCharOpt.get != grounding.head) grounding
    else grounding.drop(1)
  }
}

object HabitusGrounder {
  val defaultAddPrefixSpace: Boolean = true
  val defaultSpecialCharOpt: Option[Char] = if (defaultAddPrefixSpace) Some('\u0120') else None
  lazy val defaultTokenizer: ScalaJniTokenizer = ScalaJniTokenizer("microsoft/deberta-base", addPrefixSpace = defaultAddPrefixSpace)
  lazy val defaultWordEmbeddingMap: WordEmbeddingMap = WordEmbeddingMapPool.getOrElseCreate(
    "deberta_embd",
    compact = true,
    resourceLocation = "/org/clulab/scala_transformers/embeddings/"
  )

  def apply(wordEmbeddingMap: WordEmbeddingMap, tokenizer: Tokenizer, specialCharOpt: Option[Char]): HabitusGrounder =
      new HabitusGrounder(wordEmbeddingMap, tokenizer, specialCharOpt)

  def apply(wordEmbeddingMap: WordEmbeddingMap): HabitusGrounder =
      apply(wordEmbeddingMap, defaultTokenizer, defaultSpecialCharOpt)

  def apply(): HabitusGrounder = apply(defaultWordEmbeddingMap)
}

object HabitusGrounderApp extends App {
  val grounder = HabitusGrounder()
  val grounding = grounder.ground(Array("male", "ruler"))

  println(grounding)
}
