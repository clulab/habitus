package org.clulab.habitus.apps

import org.clulab.embeddings.{WordEmbeddingMap, WordEmbeddingMapPool}
import org.clulab.scala_transformers.tokenizer.Tokenizer
import org.clulab.scala_transformers.tokenizer.jni.ScalaJniTokenizer

// In this very simple grounder, a Seq of words is being matched with the
// single word in the embedding map keys that most closely summarizes or
// approximates them.  Perhaps "male ruler" results in "king".
class HabitusGrounder(wordEmbeddingMap: WordEmbeddingMap, tokenizer: Tokenizer) {

  def ground(words: Array[String]): String = {
    val tokens = tokenizer.tokenize(words).tokens
    val compositeVector = wordEmbeddingMap.makeCompositeVector(tokens)
    val grounding = wordEmbeddingMap.keys.maxBy { key =>
      val vector = wordEmbeddingMap.get(key).get
      val similarity = WordEmbeddingMap.dotProduct(compositeVector, vector)

      println(s"$key\t$similarity")
      similarity
    }

    grounding
  }
}

object HabitusGrounder {
  lazy val defaultTokenizer: ScalaJniTokenizer = ScalaJniTokenizer("microsoft/deberta-v3-base")
  lazy val defaultWordEmbeddingMap: WordEmbeddingMap = WordEmbeddingMapPool.getOrElseCreate(
    "deberta_embd",
    compact = true,
    resourceLocation = "/org/clulab/scala_transformers/embeddings/"
  )

  def apply(wordEmbeddingMap: WordEmbeddingMap, tokenizer: Tokenizer): HabitusGrounder =
      new HabitusGrounder(wordEmbeddingMap, tokenizer)

  def apply(wordEmbeddingMap: WordEmbeddingMap): HabitusGrounder = apply(wordEmbeddingMap, defaultTokenizer)

  def apply(): HabitusGrounder = apply(defaultWordEmbeddingMap)
}

object HabitusGrounderApp extends App {
  val grounder = HabitusGrounder()
  val grounding = grounder.ground(Array("male", "ruler"))

  println(grounding)
}
