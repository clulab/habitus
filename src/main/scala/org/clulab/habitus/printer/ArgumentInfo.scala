package org.clulab.habitus.printer

import org.clulab.odin.Mention

case class ArgumentInfo(variableText: String, valueText: String, valueNorm: String)

object ArgumentInfo {

  def apply(mention: Mention, printVariables: PrintVariables): ArgumentInfo = {
    val variableText = mention.arguments(printVariables.mentionType).head.text
    val valueMention = mention.arguments(printVariables.mentionExtractor).head
    val valueText = valueMention.text
    val valueNormsOpt = valueMention.norms
    val valueNorm =
        if (valueNormsOpt.isDefined && valueNormsOpt.get.length >= 2)
          valueNormsOpt.get.head
        else if (valueMention.words.nonEmpty)
          // Not all NEs have meaningful norms set.  For example, DATEs have norms, but CROPs do not.
          // In the latter case, we revert to the lemmas or to the actual text as a backoff.
          valueMention.words.mkString(" ")
        else
          valueText

    new ArgumentInfo(variableText, valueText, valueNorm)
  }
}
