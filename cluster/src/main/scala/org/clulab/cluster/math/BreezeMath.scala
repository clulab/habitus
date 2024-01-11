package org.clulab.cluster.math

import breeze.linalg.{DenseMatrix, DenseVector, Transpose, `*`, argmax => BreezeArgmax}

object BreezeMath /*extends Math*/ {
  type MathValue = Float
  type MathRowMatrix = DenseMatrix[MathValue]
  type MathColVector = DenseVector[MathValue]
  type MathRowVector = Transpose[DenseVector[MathValue]]
}
