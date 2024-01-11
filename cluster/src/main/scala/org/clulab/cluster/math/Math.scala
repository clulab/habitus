package org.clulab.cluster.math

trait Math {
  type MathValue
  type MathRowMatrix
  type MathColVector
  type MathRowVector

  val emptyRowVector: MathRowVector
  val emptyColVector: MathColVector
  val emptyMatrix: MathRowMatrix

  def matrixToArray(matrix: MathRowMatrix): Array[Array[MathValue]]

  def matrixInit(matrix: Array[Array[MathValue]]): MathRowMatrix
  def matrixInit(vectors: Array[MathRowVector]): MathRowMatrix
  def matrixInit(rowCount: Int, colCount: Int)(f: (Int, Int) => MathValue): MathRowMatrix

  def matrixMap(matrix: MathRowMatrix)(f: MathRowVector => MathValue): MathRowVector
  def matrixMapWithIndex(matrix: MathRowMatrix)(f: (MathRowVector, Int) => MathRowVector): MathRowMatrix

  def matrixGet(matrix: MathRowMatrix, rowIndex: Int, colIndex: Int): MathValue
  def matrixGet(matrix: MathRowMatrix, rowIndex: Int, colIndex: Int, defaultValue: MathValue): MathValue
  def matrixGetRow(matrix: MathRowMatrix, rowIndex: Int): MathRowVector
  def matrixGetCol(matrix: MathRowMatrix, colIndex: Int): MathColVector

  def rowIterator(matrix: MathRowMatrix): Iterator[MathRowVector]

  def matrixColCount(matrix: MathRowMatrix): Int
  def matrixRowCount(matrix: MathRowMatrix): Int
  def vectorColCount(vector: MathRowVector): Int
  def vectorRowCount(vector: MathColVector): Int

  def matrixAdd(left: MathRowMatrix, right: MathRowMatrix): MathRowMatrix
  def matrixAddScalar(matrix: MathRowMatrix, scalar: MathValue): MathRowMatrix
  def matrixSub(left: MathRowMatrix, right: MathRowMatrix): MathRowMatrix

  def matrixInRange(matrix: MathRowMatrix, rowIndex: Int, colIndex: Int): Boolean

  def matrixColAve(matrix: MathRowMatrix): MathRowVector
  def matrixRowAve(matrix: MathRowMatrix): MathColVector
  def matrixRowSub(vector: MathRowVector, matrix: MathRowMatrix): MathRowMatrix

  def vectorRowToArray(vector: MathRowVector): Array[MathValue]

  def vectorInit(data: Array[MathValue]): MathRowVector

  def vectorRowGet(vector: MathRowVector, index: Int): MathValue
  def vectorColGet(vector: MathColVector, index: Int): MathValue

  def vectorRecip(vector: MathRowVector): MathRowVector
  def vectorSub(left: MathRowVector, right: MathRowVector): MathRowVector

  def vectorPow(vector: MathRowVector, exponent: MathValue): MathRowVector
  def vectorMul(vector: MathRowVector, multiplicand: MathValue): MathRowVector

  def vectorDot(left: MathRowVector, right: MathRowVector): MathValue
  def vectorNorm(embedding: MathRowVector): MathValue
  def vectorRowSum(vector: MathRowVector): MathValue
  def vectorColSum(vector: MathColVector): MathValue
  def cosineDistance(left: MathRowVector, right: MathRowVector): MathValue

  def vectorMax(vector: MathColVector): MathValue
}
