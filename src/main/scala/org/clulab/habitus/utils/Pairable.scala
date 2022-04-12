package org.clulab.habitus.utils

trait Pairable {

  def getPairs: List[(String, AnyRef)] = this.getClass.getDeclaredFields.toList
      .map { declaredField =>
        declaredField.setAccessible(true)
        (declaredField.getName, declaredField.get(this))
      }

  def getNames: List[String] = getPairs.map(_._1)

  def getValues: List[AnyRef] = getPairs.map(_._2)
}
