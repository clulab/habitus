package org.clulab.habitus.actions

import org.clulab.odin.{Actions, Mention, State}

class HabitusActions extends Actions {
  //
  // local actions
  //



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
          innerBelief.sentence == outerBelief.sentence &&
          innerBelief.tokenInterval.contains(outerBelief.tokenInterval)
      }
    }

    filteredBeliefs ++ nonBeliefs
  }
}
