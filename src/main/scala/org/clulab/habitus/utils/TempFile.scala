package org.clulab.habitus.utils

import java.io.File

class TempFile(prefix: String = TempFile.getClass.getSimpleName, suffix: String = "") extends AutoCloseable {
  val file = File.createTempFile(prefix, suffix)

  def close(): Unit = file.delete()
}

object TempFile
