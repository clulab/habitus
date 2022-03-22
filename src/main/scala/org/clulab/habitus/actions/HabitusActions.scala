package org.clulab.habitus.actions

import org.clulab.odin.{Actions, EventMention, Mention, RelationMention, State, TextBoundMention, mkTokenInterval}

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
    mentions
//    cleanupAction(mentions)
//      // sorting to make sure tests that rely on mention order pass
//      .sortBy(_.sentence)
//      .sortBy(_.tokenInterval)

  def cleanupAction(mentions: Seq[Mention]): Seq[Mention] = {
    mentions
//    val r1 = removeRedundantVariableMentions(keepLongestMentions(mentions))
//    r1
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


  def copyWithArgs(orig: Mention, newArgs: Map[String, Seq[Mention]]): Mention = {
    val newTokInt = mkTokenInterval(newArgs)
    orig match {
      case tb: TextBoundMention => ???
      case rm: RelationMention => rm.copy(arguments = newArgs, tokenInterval = newTokInt)
      case em: EventMention => em.copy(arguments = newArgs, tokenInterval = newTokInt)
      case _ => ???
    }
  }


  def areaVarActionFlow(mentions: Seq[Mention]): Seq[Mention] = {
    // applies within a rule
    // split mentions with mult args into binary mentions
    val split = splitIfTwoValues(mentions)
    // filter out the ones where var and val are too far
    limitVarValSpan(split)
  }

  def limitVarValSpan(mentions: Seq[Mention]): Seq[Mention] = {
    val toReturn = new ArrayBuffer[Mention]()
    for (m <- mentions) {
      // at this point, the mentions are already binary (one var and one val)
      val args = m.arguments.map(_._2.head).toSeq
      val sortedArgs = args.sortBy(_.tokenInterval)
      val distance = sortedArgs.last.start -  sortedArgs.head.end
      if (distance <= 17) {
        toReturn.append(m)
      }
    }
    toReturn
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

  def removeRedundantVariableMentions(mentions: Seq[Mention]): Seq[Mention] = {
    // if there are multiple mentions of with the same value, pick one
    val toReturn = new ArrayBuffer[Mention]()
    val (targetMentions, other) = mentions.partition(_.label == "Assignment")
    val groupedBySent = targetMentions.groupBy(_.sentence)
    for (sentGroup <- groupedBySent) {
        val groupedByValue = sentGroup._2.groupBy(_.arguments("value").head.text)
        for (valueGroup <- groupedByValue) {
          // pick the one where the variable is closest to the value
          val menToKeep = closestVar(valueGroup._2)
          toReturn.append(menToKeep)
        }
      }
    toReturn ++ other
  }

  def distanceBetweenTwoArgs(mention: Mention, arg1: String, arg2: String): Int = {
    // assumes one arg of each type
    val sortedArgs = mention.arguments.map(_._2.head).toSeq.sortBy(_.tokenInterval.start)
    sortedArgs.last.start - sortedArgs.head.end
  }
  def closestVar(mentions: Seq[Mention]): Mention = {
    // given several var-val mentions with the same value, will keep the mention that has the variable argument
    // closest to the value
    mentions.minBy(m => distanceBetweenTwoArgs(m, "variable", "value"))
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
