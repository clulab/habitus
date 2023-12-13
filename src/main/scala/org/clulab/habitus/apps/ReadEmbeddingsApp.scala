package org.clulab.habitus.apps

import org.clulab.embeddings.WordEmbeddingMapPool

object ReadEmbeddingsApp extends App {
  val special = "\u0120"
  val wordEmbeddingMap = WordEmbeddingMapPool.getOrElseCreate("deberta_embd", compact = true, resourceLocation = "/org/clulab/scala_transformers/embeddings/")
  val embeddingOpt = wordEmbeddingMap.get(special + "what")

  assert(embeddingOpt.isDefined)
}
