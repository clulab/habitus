package org.clulab.habitus.utils

import scala.collection.mutable

object EqualityHashMap {
  type EqualityHashMap[K, V] = mutable.HashMap[K, V]
}
