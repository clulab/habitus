package org.clulab.cluster

import org.clulab.cluster.math.Maths.Math
import org.scalatest.flatspec.{AnyFlatSpec => FlatSpec}
import org.scalatest.matchers.should.Matchers

class Test extends FlatSpec with Matchers {

  def newDocument(index: Int, text: String, vector: Array[Double]): Document = {
    val document: Document = new Document(index, text, text, text.split(" "), "", "")

    document.setVector(Math.vectorInit(vector))
    document
  }
}
