package org.clulab.habitus.utils

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

class MultiCloser[T <: MultiCloser.Closeable](constructors: MultiCloser.Constructor[T]*)(implicit ev: ClassTag[T]) {
  val values: Array[T] = {
    val arrayBuffer = new ArrayBuffer[T]()

    try {
      constructors.foreach { constructor =>
        arrayBuffer += constructor()
      }
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

  val sth: Array[(=> Int)] = Array( {8},{7} )

  def close(): Unit = close(values)
}

object MultiCloser {
  type Constructor[T] = () => T // because Array[(=> T)] in not allowed
  protected type Closeable = {def close() : Unit}
}
