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
        // allowing norms for as short as one token to account for norms of multiple values sharing a unit, e.g., the norm for token 3.6 in `3.6 and 4.7 t/ha` is `3.6 t/ha`
        if (normsOpt.isDefined && normsOpt.get.nonEmpty && normsOpt.get.head.nonEmpty)
          normsOpt.get.head
        else "N/A"

    new ArgumentInfo(name, text, norm)
  }
}
