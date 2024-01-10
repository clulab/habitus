package org.clulab.cluster

class Corpus(val linguist: Linguist, preexisting: Any) {
  val documents: Seq[Document] = emptyDocuments // loadAnchoredDocuments(true)
  val vectorTexts = documents.map { document =>
    document.getVectorText
  }
  val counts: Seq[Int] = linguist.wordVectorizer.fitTransform(vectorTexts)
  val words: Seq[String] = linguist.wordVectorizer.getFeatureNamesOut
  val wordIndices: Map[String, Int] = words.zipWithIndex.toMap
  val wordsInDocs: Seq[String] = {
    val words = documents.flatMap { document =>
      document.tokens
    }

    words.toSet.toSeq
  }
  val docDistances: Any = loadDistances("filename") // TODO: type

  def loadAnchoredDocuments(load_all: Boolean): Seq[Document] = {
    ???
  }

  def loadDistances(filename: String) = {
    // np.load(filename)
    // What is in here?
    null
  }
}
