package org.clulab.habitus.utils

// Array.view(from, until) is no longer available in Scala 2.13+.
class ArrayView[T](array: Array[T], from: Int, until: Int) extends IndexedSeq[T] {
  val length: Int = until - from

  override def apply(index: Int): T = array(from + index)
}

object ArrayView {

  def apply[T](array: Array[T]): ArrayView[T] = apply(array, 0)

  def apply[T](array: Array[T], from: Int): ArrayView[T] = apply(array, from, array.length)

  def apply[T](array: Array[T], from: Int, until: Int): ArrayView[T] = new ArrayView(array, from, until)
}
