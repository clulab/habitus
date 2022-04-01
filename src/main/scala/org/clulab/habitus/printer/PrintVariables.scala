package org.clulab.habitus.printer

import scala.beans.BeanProperty

case class PrintVariables(@BeanProperty var mentionLabel: String, @BeanProperty var mentionType: String, @BeanProperty var mentionExtractor: String) {
  def this() = this("", "", "")
}
