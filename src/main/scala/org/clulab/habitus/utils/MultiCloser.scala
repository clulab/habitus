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
      case throwable: Throwable =>
        close(arrayBuffer.toArray) // Suppress any exceptions here.
        throw throwable
    }
  }

  protected def close(values: Array[T]): Array[Option[Throwable]] = {
    values.reverse.map { value =>
      try {
        value.close()
        None
      }
      catch {
        case throwable: Throwable => Some(throwable)
      }
    }.reverse
  }

  def close(): Unit = close(values)
}

object MultiCloser {
  protected type Closeable = {def close() : Unit}
}
