package org.clulab.habitus.utils

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

class MultiCloser[T <: MultiCloser.Closeable](lazies: Lazy[T]*)(implicit ev: ClassTag[T]) {
  val values: Array[T] = {
    val arrayBuffer = new ArrayBuffer[T]()

    try {
      lazies.foreach(arrayBuffer += _.value)
      arrayBuffer.toArray
    }
    catch {
      case exception: Throwable =>
        val exceptions = close(arrayBuffer.toArray).reverse // Report in opposite order of lazies.
        throw withMultiException(exception, exceptions)
    }
  }

  protected def withMultiException(throwable: Throwable, exceptions: Array[Throwable]): Throwable = {
    if (exceptions.nonEmpty) {
      val multiException = new MultiCloser.MultiException(exceptions)
      throwable.addSuppressed(multiException)
    }
    throwable
  }

  // Close in reverse order, but return exceptions in the same order as the values.
  protected def close(values: Array[T]): Array[Throwable] = {
    val result = values.reverse.map { value =>
      try {
        value.close()
        None
      }
      catch {
        case throwable: Throwable => Some(throwable)
      }
    }.reverse.flatten

    result
  }

  def close(): Unit = {
    val exceptions = close(values).reverse // Report in opposite order of lazies.

    if (exceptions.nonEmpty)
      throw withMultiException(exceptions.head, exceptions.tail)
  }
}

object MultiCloser {
  protected type Closeable = {def close() : Unit}

  class MultiException(val exceptions: Array[Throwable]) extends RuntimeException("There were problems closing.")
}
