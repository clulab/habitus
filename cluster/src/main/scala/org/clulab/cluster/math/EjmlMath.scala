package org.clulab.cluster.math

import org.ejml.data.DMatrixRMaj
import org.ejml.dense.row.{CommonOps_DDRM => Ops}

object EjmlMath extends Math {
  type MathValue = Double
  type MathRowMatrix = DMatrixRMaj
  type MathColVector = DMatrixRMaj
  type MathRowVector = DMatrixRMaj

  protected val empty: MathRowMatrix = new DMatrixRMaj()

  val emptyRowVector: MathRowVector = empty
  val emptyColVector: MathColVector = empty
  val emptyMatrix: MathRowMatrix = empty

  protected def isRowVector(rowVector: MathRowVector): Boolean = rowVector.getNumRows == 1

  protected def isColVector(colVector: MathColVector): Boolean = colVector.getNumCols == 1

  override def matrixToArray(matrix: MathRowMatrix): Array[Array[MathValue]] = {
    val colCount = matrix.getNumCols
    val data = matrix.getData
    var pos = 0

    Array.fill[Array[MathValue]](matrix.getNumRows) {
      val row = new Array[MathValue](colCount)

      System.arraycopy(data, pos, row, 0, colCount)
      pos += colCount
      row
    }
  }

  override def matrixInit(array2d: Array[Array[MathValue]]): MathRowMatrix = {
    new DMatrixRMaj(array2d)
  }

  override def matrixInit(vectors: Array[MathRowVector]): MathRowMatrix = {
    val rowCount = vectors.length
    val colCount = vectors.head.getNumCols
    val newData = new Array[MathValue](rowCount * colCount)
    var pos = 0

    vectors.foreach { vector =>
      System.arraycopy(vector.getData, 0, newData, pos, colCount)
      pos += colCount
    }
    DMatrixRMaj.wrap(rowCount, colCount, newData)
  }

  override def matrixInit(rowCount: Int, colCount: Int)(f: (Int, Int) => MathValue): MathRowMatrix = {
    val newData = new Array[MathValue](rowCount * colCount)
    var pos = 0

    0.until(rowCount).foreach { rowIndex =>
      0.until(colCount).foreach { colIndex =>
        newData(pos) = f(rowIndex, colIndex)
        pos += 1
      }
    }
    DMatrixRMaj.wrap(rowCount, colCount, newData)
  }

  override def matrixMap(matrix: MathRowMatrix)(f: MathRowVector => Double): MathRowVector = {
    val rowCount = matrix.getNumRows
    val colCount = matrix.getNumCols
    val data = matrix.getData
    val newRowData = new Array[MathValue](rowCount)
    val tmpRowData = new Array[MathValue](colCount)
    val rowVector = DMatrixRMaj.wrap(1, colCount, tmpRowData)
    var pos = 0

    0.until(rowCount).foreach { rowIndex =>
      System.arraycopy(data, pos, tmpRowData, 0, colCount)
      pos += colCount
      newRowData(rowIndex) = f(rowVector)
    }
    DMatrixRMaj.wrap(1, rowCount, newRowData)
  }

  override def matrixMapWithIndex(matrix: MathRowMatrix)(f: (MathRowVector, Int) => MathRowVector): MathRowMatrix = {
    val rowCount = matrix.getNumRows
    val colCount = matrix.getNumCols
    val data = matrix.getData
    val tmpRowData = new Array[MathValue](colCount)
    val rowVector = DMatrixRMaj.wrap(1, colCount, tmpRowData)
    var pos = 0
    val newRowVectors = 0.until(rowCount).toArray.map { rowIndex =>
      System.arraycopy(data, pos, tmpRowData, 0, colCount)

      val result = f(rowVector, rowIndex)

      pos += colCount
      result
    }

    matrixInit(newRowVectors)
  }

  override def matrixGet(matrix: MathRowMatrix, rowIndex: Int, colIndex: Int): MathValue = {
    matrix.get(rowIndex, colIndex)
  }

  override def matrixGet(matrix: MathRowMatrix, rowIndex: Int, colIndex: Int, defaultValue: MathValue): MathValue = {
    if (matrixInRange(matrix, rowIndex, colIndex))
      matrix.get(rowIndex, colIndex)
    else
      defaultValue
  }

  override def matrixGetRow(matrix: MathRowMatrix, rowIndex: Int): MathRowVector = {
    Ops.extract(matrix, rowIndex, rowIndex + 1, 0, matrix.getNumCols)
  }

  override def matrixGetCol(matrix: MathRowMatrix, colIndex: Int): MathColVector = {
    Ops.extract(matrix, 0, matrix.getNumRows, colIndex, colIndex + 1)
  }

  class MathRowVectorIterator(matrix: MathRowMatrix) extends Iterator[MathRowVector] {
    protected val rowCount: Int = matrix.getNumRows
    protected var currentRowIndex: Int = 0

    override def hasNext: Boolean = currentRowIndex < rowCount

    override def next(): MathRowVector = {
      val result = matrixGetRow(matrix, currentRowIndex)

      currentRowIndex += 1
      result
    }
  }

  override def rowIterator(matrix: MathRowMatrix): Iterator[MathRowVector] = {
    new MathRowVectorIterator(matrix)
  }

  override def matrixColCount(matrix: MathRowMatrix): Int = matrix.getNumCols

  override def matrixRowCount(matrix: MathRowMatrix): Int = matrix.getNumRows

  override def vectorColCount(vector: MathRowVector): Int = vector.numCols

  override def vectorRowCount(vector: MathColVector): Int = vector.numRows

  override def matrixAdd(left: MathRowMatrix, right: MathRowMatrix): MathRowMatrix = {
    Ops.add(left, right, null)
  }

  override def matrixAddScalar(matrix: MathRowMatrix, scalar: MathValue): MathRowMatrix = {
    Ops.add(matrix, scalar, null)
  }

  override def matrixSub(left: MathRowMatrix, right: MathRowMatrix): MathRowMatrix = {
    Ops.subtract(left, right, null)
  }

  override def matrixInRange(matrix: MathRowMatrix, rowIndex: Int, colIndex: Int): Boolean = {
    0 <= rowIndex && rowIndex < matrix.getNumRows &&
    0 <= colIndex && colIndex < matrix.getNumCols
  }

  override def matrixColAve(matrix: MathRowMatrix): MathRowVector = {
    val result = Ops.sumCols(matrix, null)

    Ops.divide(result, matrix.getNumRows)
    result
  }

  override def matrixRowAve(matrix: MathRowMatrix): MathRowMatrix = {
    val result = Ops.sumRows(matrix, null)

    Ops.divide(result, matrix.getNumCols)
    result
  }

  override def matrixRowSub(vector: MathRowVector, matrix: MathRowMatrix): MathRowMatrix = {
    // for each row of matrix, do vector - row
    val rowCount = matrix.getNumRows
    val newMatrix = new DMatrixRMaj(rowCount, matrix.getNumCols)

    0.until(rowCount).foreach { rowIndex =>
      val row = Ops.extractRow(matrix, rowIndex, null)

      Ops.subtract(vector, row, row)
      Ops.insert(row, newMatrix, rowIndex, 0)
    }
    newMatrix
  }

  override def vectorRowToArray(vector: MathRowVector): Array[MathValue] = vector.getData

  override def vectorInit(data: Array[MathValue]): MathRowVector = DMatrixRMaj.wrap(1, data.length, data)

  override def vectorRowGet(vector: MathRowVector, index: Int): MathValue = vector.get(index)

  override def vectorColGet(vector: MathColVector, index: Int): MathValue = vector.get(index)

  override def vectorRecip(vector: MathRowVector): MathRowVector = Ops.elementPower(vector, -1d, null)

  override def vectorSub(left: MathRowVector, right: MathRowVector): MathRowVector = Ops.subtract(left, right, null)

  override def vectorPow(vector: MathRowVector, exponent: MathValue): MathRowVector = Ops.elementPower(vector, exponent, null)

  override def vectorMul(vector: MathRowVector, multiplicand: MathValue): MathRowVector = {
    val result = new DMatrixRMaj(vector.getNumRows, vector.getNumCols)

    Ops.scale(multiplicand, vector, result)
    result
  }

  override def vectorDot(left: MathRowVector, right: MathRowVector): MathValue = Ops.dot(left, right)

  override def vectorNorm(embedding: MathRowVector): MathValue = {
    val sumOfSquares = Ops.dot(embedding, embedding)

    math.sqrt(sumOfSquares)
  }

  override def vectorRowSum(vector: MathRowVector): MathValue = Ops.elementSum(vector)

  override def vectorColSum(vector: MathColVector): MathValue = Ops.elementSum(vector)

  override def cosineDistance(left: MathRowVector, right: MathRowVector): MathValue =
    1d - vectorDot(left, right) / vectorNorm(left) / vectorNorm(right)

  override def vectorMax(vector: MathColVector): MathValue = Ops.elementMax(vector)
}
