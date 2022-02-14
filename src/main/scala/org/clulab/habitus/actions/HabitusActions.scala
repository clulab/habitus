package org.clulab.habitus.actions

import org.clulab.odin.{Actions, EventMention, Mention, RelationMention, State, TextBoundMention}

import scala.collection.mutable.ArrayBuffer

class HabitusActions extends Actions {
  //
  // local actions
  //
  def uniqueArguments(mentions: Seq[Mention]): Seq[Mention] = {
    // filter out mentions where one mention acts as two types of argument (e.g., one tbd extracted as both believer and belief arg)
    val toReturn = new ArrayBuffer[Mention]()
    for (m <- mentions) {
      val allArgs = m.arguments.values.flatten.toSeq
      if (allArgs.distinct.length == allArgs.length) {
        toReturn.append(m)
      }
    }
    toReturn
  }


  //
  // global actions below this points
  //

  /** Global action for the numeric grammar */
  def cleanupAction(mentions: Seq[Mention], state: State): Seq[Mention] =
    cleanupAction(mentions)


  def cleanupAction(mentions: Seq[Mention]): Seq[Mention] = {
    val r1 = keepLongestMentions(mentions)
    r1
  }

  private def isBelief(m: Mention): Boolean = {
    m.labels.contains("Belief")
  }

  /** Keeps a belief mention only if it is not contained in another */
  def keepLongestMentions(mentions: Seq[Mention]): Seq[Mention] = {
    val (beliefs, nonBeliefs) = mentions.partition(isBelief)
    val filteredBeliefs = beliefs.filterNot { outerBelief =>
      beliefs.exists { innerBelief =>
        innerBelief != outerBelief &&
          innerBelief.tokenInterval != outerBelief.tokenInterval && // to avoid filtering of same span mentions
          innerBelief.sentence == outerBelief.sentence &&
          innerBelief.tokenInterval.contains(outerBelief.tokenInterval)
      }
    }
    keepOneOfSameSpan(uniqueArguments(filteredBeliefs)) ++ nonBeliefs
  }

  def copyWithArgs(orig: Mention, newArgs: Map[String, Seq[Mention]]): Mention = {
    orig match {
      case tb: TextBoundMention => ???
      case rm: RelationMention => rm.copy(arguments = newArgs)
      case em: EventMention => em.copy(arguments = newArgs)
      case _ => ???
    }
  }

  def splitIfTwoValues(mentions: Seq[Mention]): Seq[Mention] = {
    // for area rules; if there is an extraction with multiple value args,
    // split it into binary var-value mentions
    val (assignmentMentions, other) = mentions.partition(_ matches "Assignment")
    val toReturn = new ArrayBuffer[Mention]()
    for (am <- assignmentMentions) {
      val valueArgs = am.arguments("value")
      if (valueArgs.length > 1) {
        for (valueArg <- valueArgs) {
          val newArgs = Map("variable" -> Seq(am.arguments("variable").head), "value" -> Seq(valueArg))
          toReturn.append(copyWithArgs(am, newArgs))
        }
      } else toReturn.append(am)
    }
    toReturn ++ other
  }


  def keepOneOfSameSpan(mentions: Seq[Mention]): Seq[Mention] = {
    // if there are two mentions of same span and label, keep one
    val toReturn = new ArrayBuffer[Mention]()
    val groupedBySent = mentions.groupBy(_.sentence)
    for (sentGroup <- groupedBySent) {
      val groupedByLabel = sentGroup._2.groupBy(_.label)
      for (labelGroup <- groupedByLabel) {
        val groupedBySpan = labelGroup._2.groupBy(_.tokenInterval)
        for (spanGroup <- groupedBySpan) {
          // pick the one that has most args to avoid filtering out mentions that allow for
          // more than one arg of the same type
          //todo: add 'by longest arg span' as an alternative way to pick which overlapping mention to keep?
          val menToKeep = spanGroup._2.maxBy(_.arguments.values.flatten.toSeq.length)
          toReturn.append(menToKeep)
        }
      }
    }
    toReturn
  }
}
