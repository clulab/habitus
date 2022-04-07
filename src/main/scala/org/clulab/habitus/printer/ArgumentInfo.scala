package org.clulab.habitus.printer

import org.clulab.habitus.utils.Pairable
import org.clulab.odin.Mention

case class ArgumentInfo(name: String, text: String, norm: String) extends Pairable

object ArgumentInfo {

  def apply(name: String, mention: Mention): ArgumentInfo = {
    val text = mention.arguments(name).head.text
    val normsOpt = mention.arguments(name).head.norms
    val norm =
        // Not all NEs have meaningful norms set.  For example, DATEs have norms, but CROPs do not.
        if (normsOpt.isDefined && normsOpt.get.length >= 2 && normsOpt.get.head.nonEmpty)
          normsOpt.get.head
        else "N/A"

    new ArgumentInfo(name, text, norm)
  }
}
