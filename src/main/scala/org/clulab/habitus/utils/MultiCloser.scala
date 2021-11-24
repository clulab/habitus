package org.clulab.habitus.utils

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

class MultiCloser[T <: MultiCloser.Closeable](values: MultiCloser.Constructor[T]*)(implicit ev: ClassTag[T]) {
  val constructeds: Array[T] = {
    val arrayBuffer = new ArrayBuffer[T]()

    try {
      values.foreach { value =>
        arrayBuffer += value()
      }
      arrayBuffer.toArray
    }
    catch {
      case throwable: Throwable =>
        close(arrayBuffer.toArray) // Suppress any exceptions here.
        throw throwable
    }
  }

  protected def close(constructeds: Array[T]): Array[Option[Throwable]] = {
    constructeds.reverse.map { constructed =>
      try {
        constructed.close()
        None
      }
      catch {
        case throwable: Throwable => Some(throwable)
      }
    }.reverse
  }

  def close(): Unit = close(constructeds)
}

object MultiCloser {
  type Constructor[T] = () => T
  protected type Closeable = {def close() : Unit}
}
