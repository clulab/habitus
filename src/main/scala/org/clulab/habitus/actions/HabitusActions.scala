package org.clulab.habitus.actions

import org.clulab.odin.{Actions, Attachment, EventMention, Mention, RelationMention, State, SynPath, TextBoundMention, mkTokenInterval}
import org.clulab.processors.Document
import org.clulab.struct.Interval

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
      // sorting to make sure tests that rely on mention order pass
      .sortBy(_.sentence)
      .sortBy(_.tokenInterval)

  def cleanupAction(mentions: Seq[Mention]): Seq[Mention] = {
    val r1 = removeRedundantVariableMentions(keepLongestMentions(mentions))
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
    // The action makes sure there is one variable for every value in most assignment events/relations (those that have value args); we exclude property assignments from this because one property can apply to multiple variables (e.g., They planted crop1 and crop2 (short duration))
    // The action applies to mentions extracted with var reader relation/event rules, so we exclude TBMs and include a value argument.
    val (targetMentions, otherMentions) = mentions.partition(m => !m.isInstanceOf[TextBoundMention] && m.arguments.contains("value") && m.label != "PropertyAssignment")
    val targetMentionGroups = targetMentions.groupBy { m => (m.sentence, m.label, m.arguments("value").head.text) }
    // If there are multiple mentions in the same group, pick the "closest" one.
    val closestTargetMentions = targetMentionGroups.toSeq.map { case (_, ms) => closestVar(ms) }

    closestTargetMentions ++ otherMentions
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

  def fertilizerEventToRelation(mentions: Seq[Mention]): Seq[Mention] = {
    // creates a binary fertilizer event from a rule where the trigger was a token that should also serve as a variable in an assignment event
    mentions.map { m =>
      val variableArg = m.asInstanceOf[EventMention].trigger
      val valueArg = m.arguments("value").head
      val sortedArgs = Seq(variableArg, valueArg).sortBy(_.tokenInterval)
      val newTokenInterval = Interval(sortedArgs.head.tokenInterval.start, sortedArgs.last.tokenInterval.end)
      new RelationMention(
        labels = m.labels,
        tokenInterval = newTokenInterval,
        arguments = Map("variable" -> Seq(variableArg), "value" -> Seq(valueArg)),
        paths = m.paths,
        sentence = m.sentence,
        document = m.document,
        keep = m.keep,
        foundBy = m.foundBy,
        attachments = Set.empty
      )
    }
  }
  def splitIntoBinary(mentions: Seq[Mention]): Seq[Mention] = {
    println("HERE " + mentions.length)
    for (m <- mentions) {
      println("MM: " + m.label + " " + m.text)
      for (a <- m.arguments) {
        for (aa <- a._2) println("\t" + a._1 + " " + aa.label + " " + aa.text)
      }
    }
    val (targets, other) = mentions.partition( m => {
      val valueLabels = m.arguments("value").map(_.label)
      valueLabels.length > 1 &&
      valueLabels.distinct.length == 1
      }
    )
    println("LEN TARGETS: " + targets.length)

    for (m <- targets) {
      println("M: " + m.label + " " + m.text)
      for (a <- m.arguments) {
        for (aa <- a._2) println(a._1 + " " + " " + aa.label +" " + aa.text )
      }
    }
    val splitTargets = for {
      m <- targets
      value <- m.arguments("value")
      newArgs = Map("variable" -> m.arguments("variable"), "value" -> Seq(value))
    } yield copyWithArgs(m, newArgs)
    splitTargets ++ other
  }

  val labelToAppropriateUnits = Map(
    "Quantity" -> Set("t/ha", "kg/ha", "kg", "d", "cm", "mg/l"),
    "AreaSize" -> Set("ha")
  )

  def measurementIsAppropriate(m: Mention): Boolean = {
    // check if any of the possible units shows up in the norm
    val probablyUnit = m.norms.get.head.split("\\s").last // e.g., get "m2" from "6 m2"; assume value is separated from unit with a space and unit is one token
    labelToAppropriateUnits
      .getOrElse(m.label, throw new RuntimeException(s"Unknown measurement label ${m.label}"))(probablyUnit)
  }

  def appropriateMeasurement(mentions: Seq[Mention]): Seq[Mention] = {
    // applies to individual rules to check if the measurement is appropriate for the mention label
    mentions.filter(measurementIsAppropriate)
  }

}
