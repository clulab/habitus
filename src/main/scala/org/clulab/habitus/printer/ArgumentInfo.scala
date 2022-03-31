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
        // Not all NEs have meaningful norms set.  For example, DATEs have norms, but CROPs do not.
        if (valueNormsOpt.isDefined && valueNormsOpt.get.length >= 2 && valueNormsOpt.get.head.nonEmpty)
          valueNormsOpt.get.head
        else "N/A"

    new ArgumentInfo(variableText, valueText, valueNorm)
  }
}
