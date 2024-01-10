package org.clulab.cluster

import org.clulab.cluster.math.Maths.Math

object Types {
  type Vector[T] = Array[T]
  type Matrix[T] = Vector[Vector[T]]

  type Cluster = Vector[Document]
  // This is then a Vector[Vector[Document]], but it is jagged rather than rectangular.
  type Clusters = Vector[Cluster]

  // This is then a Vector[Vector[clusterIndex]], but it is jagged rather than rectangular.
  type LabelGroup = Vector[Int]
  type LabelGroups = Vector[LabelGroup]

  type Documents = Vector[Document]

  type Embedding = Math.MathRowVector
  // These should be rectangular.
  type Embeddings = Vector[Embedding]

  type VectorOfInt = Vector[Int]
  type VectorOfDouble = Vector[Double]

  // This should be rectangular.
  type MatrixOfInt = Matrix[Int]
  type MatrixOfDouble = Matrix[Double]
}
