package org.clulab.cluster.math

object CluMath extends Math {

  // The use of a template seems to prevent the generation of an apply method
  // so that the construction of CluMatrix requires new.  However, the
  // arguments are automatically converted into accessible vals.
  case class CluMatrix[T](rowCount: Int, colCount: Int, data: Array[Array[T]])

  sealed trait Orientation

  final class RowOrientation extends Orientation
  final class ColOrientation extends Orientation

  // This <: prevents other kinds of CluVectors from being imagined.
  // The U isn't used, but it does result in the CluRowMatrix and
  // CluColMatrix being different types and not interchangeable.
  case class CluVector[U <: Orientation, T](count: Int, data: Array[T])

  type ColVector[T] = CluVector[ColOrientation, T]
  type RowVector[T] = CluVector[RowOrientation, T]

  type MathValue = Double

  type CluRowMatrix = CluMatrix[MathValue]
  type CluColVector = ColVector[MathValue]
  type CluRowVector = RowVector[MathValue]

  type MathRowMatrix = CluRowMatrix
  type MathColVector = CluColVector
  type MathRowVector = CluRowVector

  override val emptyRowVector: MathRowVector = new CluRowVector(0, Array.empty[MathValue])
  override val emptyColVector: MathColVector = new CluColVector(0, Array.empty[MathValue])
  override val emptyMatrix: MathRowMatrix = new CluRowMatrix(0, 0, Array.empty[Array[MathValue]])

  override def matrixToArray(matrix: MathRowMatrix): Array[Array[MathValue]] = matrix.data

  override def matrixInit(array2d: Array[Array[MathValue]]): MathRowMatrix = {
    val rowCount = array2d.length
    val colCount = array2d.head.length

    new CluRowMatrix(rowCount, colCount, array2d)
  }

  override def matrixInit(vectors: Array[MathRowVector]): MathRowMatrix = {
    val rowCount = vectors.length
    val colCount = vectors.head.count
    val array2d = vectors.map(_.data)

    new CluRowMatrix(rowCount, colCount, array2d)
  }

  override def matrixInit(rowCount: Int, colCount: Int)(f: (Int, Int) => MathValue): MathRowMatrix = {
    val data = Array.tabulate[MathValue](rowCount, colCount) { case (rowIndex, colIndex) =>
      f(rowIndex, colIndex)
    }

    new CluRowMatrix(rowCount, colCount, data)
  }

  override def matrixMap(matrix: MathRowMatrix)(f: MathRowVector => MathValue): MathRowVector = {
    val colCount = matrix.colCount
    val rowCount = matrix.rowCount
    val data = matrix.data.map { data =>
      val rowVector = new MathRowVector(colCount, data)

      f(rowVector)
    }

    new CluRowVector(rowCount, data)
  }

  override def matrixMapWithIndex(matrix: MathRowMatrix)(f: (MathRowVector, Int) => MathRowVector): MathRowMatrix = {
    val colCount = matrix.colCount
    val rowData = matrix.data.zipWithIndex.map { case (data, rowIndex) =>
      val rowVector = new MathRowVector(colCount, data)

      f(rowVector, rowIndex).data
    }

    matrixInit(rowData)
  }

  override def matrixGet(matrix: MathRowMatrix, rowIndex: Int, colIndex: Int): MathValue = {
    matrix.data(rowIndex)(colIndex)
  }

  override def matrixGet(matrix: MathRowMatrix, rowIndex: Int, colIndex: Int, defaultValue: MathValue): MathValue = {
    if (matrixInRange(matrix, rowIndex, colIndex))
      matrix.data(rowIndex)(colIndex)
    else
      defaultValue
  }

  override def matrixGetRow(matrix: MathRowMatrix, rowIndex: Int): MathRowVector = {
    new MathRowVector(matrix.rowCount, matrix.data(rowIndex))
  }

  override def matrixGetCol(matrix: MathRowMatrix, colIndex: Int): MathColVector = {
    val data = matrix.data.map { vector =>
      vector(colIndex)
    }

    new MathColVector(matrix.rowCount, data)
  }

  override def rowIterator(matrix: MathRowMatrix): Iterator[MathRowVector] = {
    val colCount = matrix.colCount
    val rows = matrix.data.map { data => new CluRowVector(colCount, data) }

    rows.iterator
  }

  override def matrixColCount(matrix: MathRowMatrix): Int = matrix.colCount

  override def matrixRowCount(matrix: MathRowMatrix): Int = matrix.rowCount

  override def vectorColCount(vector: MathRowVector): Int = vector.count

  override def vectorRowCount(vector: MathColVector): Int = vector.count

  // TODO: Also, can sum be put into either side?
  override def matrixAdd(left: MathRowMatrix, right: MathRowMatrix): MathRowMatrix = {
    val rowCount = left.rowCount
    val colCount = left.colCount
    val data = Array.tabulate[MathValue](rowCount, colCount) { (rowIndex, colIndex) =>
      left.data(rowIndex)(colIndex) + right.data(rowIndex)(colIndex)
    }

    new CluRowMatrix(rowCount, colCount, data)
  }

  // TODO: Can this be added in place?
  override def matrixAddScalar(matrix: MathRowMatrix, scalar: MathValue): MathRowMatrix = {
    val rowCount = matrix.rowCount
    val colCount = matrix.colCount
    val data = Array.tabulate[MathValue](rowCount, colCount) { (rowIndex, colIndex) =>
      matrix.data(rowIndex)(colIndex) + scalar
    }

    new CluRowMatrix(rowCount, colCount, data)
  }

  override def matrixSub(left: MathRowMatrix, right: MathRowMatrix): MathRowMatrix = {
    val rowCount = left.rowCount
    val colCount = left.colCount
    val data = Array.tabulate[MathValue](rowCount, colCount) { (rowIndex, colIndex) =>
      left.data(rowIndex)(colIndex) - right.data(rowIndex)(colIndex)
    }

    new CluRowMatrix(rowCount, colCount, data)
  }

  override def matrixInRange(matrix: MathRowMatrix, rowIndex: Int, colIndex: Int): Boolean = {
    0 <= rowIndex && rowIndex < matrix.rowCount &&
    0 <= colIndex && colIndex < matrix.colCount
  }

  override def matrixColAve(matrix: MathRowMatrix): MathRowVector = {
    val rowCount = matrix.rowCount
    val colCount = matrix.colCount
    val data = Range(0, colCount).map { colIndex =>
      var sum = 0d

      Range(0, rowCount).foreach { rowIndex =>
        sum += matrix.data(rowIndex)(colIndex)
      }
      sum / rowCount
    }.toArray

    new MathRowVector(colCount, data)
  }

  override def matrixRowAve(matrix: MathRowMatrix): MathColVector = {
    val rowCount = matrix.rowCount
    val data = matrix.data.map { rowVector =>
      rowVector.sum / rowVector.length
    }

    new MathColVector(rowCount, data)
  }

  override def vectorRowToArray(vector: MathRowVector): Array[MathValue] = vector.data

  override def vectorInit(data: Array[MathValue]): MathRowVector = new CluRowVector(data.length, data)

  override def vectorRowGet(vector: MathRowVector, index: Int): MathValue = vector.data(index)

  override def vectorColGet(vector: MathColVector, index: Int): MathValue = vector.data(index)

  // Subtract vector from every row of matrix.
  override def matrixRowSub(vector: MathRowVector, matrix: MathRowMatrix): MathRowMatrix = {
    val rowCount = matrix.rowCount
    val colCount = matrix.colCount
    val data = matrix.data.map { rowVector =>
      vectorSub(vector, new CluRowVector(colCount, rowVector)).data
    }

    new CluRowMatrix(rowCount, colCount, data)
  }

  override def vectorRecip(vector: MathRowVector): MathRowVector = {
    val data = vector.data.map(1d / _)

    new RowVector(vector.count, data)
  }

  override def vectorSub(left: MathRowVector, right: MathRowVector): MathRowVector = {
    val count = left.count
    val data = Array.tabulate[MathValue](count) { index =>
      left.data(index) - right.data(index)
    }

    new RowVector(count, data)
  }

  override def vectorPow(vector: MathRowVector, exponent: MathValue): MathRowVector = {
    val data = vector.data.map(math.pow(_, exponent))

    new RowVector(vector.count, data)
  }

  override def vectorMul(vector: MathRowVector, multiplicand: MathValue): MathRowVector = {
    val data = vector.data.map(_ * multiplicand)

    new RowVector(vector.count, data)
  }

  override def vectorDot(left: MathRowVector, right: MathRowVector): MathValue = {
    val count = left.count
    var sum = 0d

    Range(0, count).foreach { index =>
      sum += left.data(index) * right.data(index)
    }
    sum
  }

  override def vectorNorm(vector: MathRowVector): MathValue = {
    val sumOfSquares = vectorDot(vector, vector)

    math.sqrt(sumOfSquares)
  }

  override def vectorRowSum(vector: MathRowVector): MathValue = {
    vector.data.sum
  }

  override def vectorColSum(vector: MathColVector): MathValue = {
    vector.data.sum
  }

  override def cosineDistance(left: MathRowVector, right: MathRowVector): MathValue = {
    1d - vectorDot(left, right) / vectorNorm(left) / vectorNorm(right)
  }

  override def vectorMax(vector: MathColVector): MathValue = vector.data.max
}
