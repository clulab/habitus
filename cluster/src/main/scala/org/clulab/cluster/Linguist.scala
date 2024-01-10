package org.clulab.cluster


class WordTokenize {

}

class CountVectorizer(analyzer: String, tokenize: WordTokenize) {
  def vocabulary(word: String) = ???
  def fitTransform(texts: Seq[String]) = Seq.empty[Int]
  def getFeatureNamesOut(): Seq[String] = Seq.empty[String]
}


class Linguist {
  val wordVectorizer: CountVectorizer = new CountVectorizer("word", new WordTokenize())
}
