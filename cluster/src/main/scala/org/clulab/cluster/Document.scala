package org.clulab.cluster

import org.clulab.cluster.math.Maths.Math
import org.clulab.cluster.Types.Embedding

class Document(val index: Int, val stripped: String, val readable: String, val tokens: Seq[String], preContext: String,
    postContext: String, memberships: Seq[Boolean] = Seq.empty) {
  var vector: Embedding = Math.emptyRowVector

  def getVectorText(): String = stripped

  def setVector(vector: Embedding): Unit = this.vector = vector

  override def hashCode: Int = index

  override def equals(other: Any): Boolean =
      other.asInstanceOf[Document].index == index
}
