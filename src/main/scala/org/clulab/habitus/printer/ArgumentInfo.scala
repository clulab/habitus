package org.clulab.habitus.printer

import org.clulab.habitus.utils.Pairable
import org.clulab.odin.Mention

case class ArgumentInfo(name: String, text: String, norm: String) extends Pairable

object ArgumentInfo {
  // This needs to be coordinated with the class above.  It makes it possible for
  // the TsvPrinter to skip over arguments that aren't there in order to keep all
  // the like arguments (having the same name) in the same column.
  val width = 3

  def apply(name: String, mention: Mention): ArgumentInfo = {
    val text = mention.text
    val normsOpt = mention.norms
    val norm =
        // Not all NEs have meaningful norms set.  For example, DATEs have norms, but CROPs do not.
        if (normsOpt.isDefined && normsOpt.get.length >= 2 && normsOpt.get.head.nonEmpty)
          normsOpt.get.head
        else "N/A"

    new ArgumentInfo(name, text, norm)
  }
}
