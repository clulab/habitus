package org.clulab.habitus.actions

import org.clulab.odin.{Actions, Mention, State}

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

    keepOneOfSameSpan(uniqueArguments(filteredBeliefs ++ nonBeliefs))
  }

  def keepOneOfSameSpan(mentions: Seq[Mention]): Seq[Mention] = {
    // if there are two mentions of same span and label, keep one
    // fixme: maybe pick one with longer arg spans
    val toReturn = new ArrayBuffer[Mention]()
    val groupedBySent = mentions.groupBy(_.sentence)
    for (sentGroup <- groupedBySent) {
      val groupedByLabel = sentGroup._2.groupBy(_.label)
      for (labelGroup <- groupedByLabel) {
        val groupedBySpan = labelGroup._2.groupBy(_.tokenInterval)
        for (spanGroup <- groupedBySpan) {
          toReturn.append(spanGroup._2.head)
        }
      }
    }
    toReturn
  }
}
