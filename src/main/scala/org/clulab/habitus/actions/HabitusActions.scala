package org.clulab.habitus.actions

import org.clulab.numeric.mentions.MeasurementMention
import org.clulab.odin.{Actions, EventMention, Mention, RelationMention, State, TextBoundMention, mkTokenInterval}
import org.clulab.struct.Interval
import org.clulab.wm.eidos.expansion.TextBoundExpander

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
    // do the entity check: only use entities that occur more than once within the text---doing this to avoid location/fertilizer false pos;
    // only apply the check to documents of more than 6 sentences (paragraph length-ish?) - if we run the shell or tests,
    // there won't be enough text to do the check
    val afterEntityUniquenessCheck = if (mentions.nonEmpty && mentions.head.document.sentences.length > 6) doEntityUniquenessCheck(mentions) else mentions
    val r1 = removeRedundantVariableMentions(keepLongestMentions(afterEntityUniquenessCheck))
    r1
  }

  def doEntityUniquenessCheck(mentions: Seq[Mention]): Seq[Mention] = {
    val (entitiesToDoubleCheck, other) = mentions.partition(m =>
      m.isInstanceOf[TextBoundMention]
        && (m.label == "Location" || m.label == "Fertilizer"))
    val doubleCheckedEntities = if (entitiesToDoubleCheck.nonEmpty) returnNonUniqueEntities(entitiesToDoubleCheck) else Seq.empty
    doubleCheckedEntities ++ other
  }

  def returnNonUniqueEntities(mentions: Seq[Mention]): Seq[Mention] = {
    val groupedByLabel = mentions.groupBy(_.label)
    groupedByLabel.flatMap(gr => filterUniqTextMentionsOfSameLabel(gr._2)).toSeq
  }

  def filterUniqTextMentionsOfSameLabel(mentions: Seq[Mention]): Seq[Mention] = {
    // in this method, all mentions already have the same label
    val uniqTexts = mentions.groupBy(_.text).filter(_._2.length == 1).keys.toSeq
    mentions.filterNot(m => uniqTexts.contains(m.text))
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
  def copyWithArgs(orig: Mention, newArgs: Map[String, Seq[Mention]], foundBy: String): Mention = {
    val newTokInt = mkTokenInterval(newArgs)
    orig match {
      case tb: TextBoundMention => ???
      case rm: RelationMention => rm.copy(arguments = newArgs, tokenInterval = newTokInt, foundBy = foundBy)
      case em: EventMention => em.copy(arguments = newArgs, tokenInterval = newTokInt, foundBy = foundBy)
      case _ => ???
    }
  }
  def areaVarActionFlow(mentions: Seq[Mention]): Seq[Mention] = {
    // applies within a rule
    // split mentions with mult args into binary mentions
    val split = splitIntoBinary(mentions)
    // filter out the ones where var and val are too far
    limitVarValSpan(split)
  }
  def limitVarValSpan(mentions: Seq[Mention]): Seq[Mention] = {
    val toReturn = new ArrayBuffer[Mention]()
    for (m <- mentions) {
      // at this point, the mentions are already binary (one var and one val)
      val args = m.arguments.map(_._2.head).toSeq
      val sortedArgs = args.sortBy(_.tokenInterval)
      val distance = sortedArgs.last.start - sortedArgs.head.end
      if (distance <= 23) {
        toReturn.append(m)
      }
    }
    toReturn
  }

  val VALUE = "value"
  val VARIABLE = "variable"

//  def splitIfTwoValues(mentions: Seq[Mention]): Seq[Mention] = {
//    // for area rules; if there is an extraction with multiple value args,
//    // split it into binary var-value mentions
//    val (assignmentMentions, other) = mentions.partition(_ matches "Assignment")
//    val toReturn = new ArrayBuffer[Mention]()
//    for (am <- assignmentMentions) {
//      val valueArgs = am.arguments(VALUE)
//      if (valueArgs.length > 1) {
//        for (valueArg <- valueArgs) {
//          val newArgs = Map(VARIABLE -> Seq(am.arguments(VARIABLE).head), VALUE -> Seq(valueArg))
//          toReturn.append(copyWithArgs(am, newArgs))
//        }
//      } else toReturn.append(am)
//    }
//    toReturn ++ other
//  }
  def removeRedundantVariableMentions(mentions: Seq[Mention]): Seq[Mention] = {
    // The action makes sure there is one variable for every value in most assignment events/relations (those that have value args); we exclude property assignments from this because one property can apply to multiple variables (e.g., They planted crop1 and crop2 (short duration))
    // The action applies to mentions extracted with var reader relation/event rules, so we exclude TBMs and include a value argument.
    val (targetMentions, otherMentions) = mentions.partition(m => !m.isInstanceOf[TextBoundMention] && m.arguments.contains(VALUE) && m.label != "PropertyAssignment")
    val targetMentionGroups = targetMentions.groupBy { m => (m.sentence, m.label, m.arguments(VALUE).head.text) }
    // If there are multiple mentions in the same group, pick the "closest" one.
    val closestTargetMentions = targetMentionGroups.toSeq.map { case (_, ms) => closestVar(ms) }

    closestTargetMentions ++ otherMentions
  }
  def distanceBetweenTwoArgs(mention: Mention, arg1: String, arg2: String): Int = {
    // assumes one arg of each type
    val sortedArgs = Seq(mention.arguments(arg1).head, mention.arguments(arg2).head).sortBy(_.tokenInterval.start)
    sortedArgs.last.start - sortedArgs.head.end
  }
  def closestVar(mentions: Seq[Mention]): Mention = {
    // given several var-val mentions with the same value, will keep the mention that has the variable argument
    // closest to the value
    mentions.minBy(m => distanceBetweenTwoArgs(m, VARIABLE, VALUE))
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
      val valueArg = m.arguments(VALUE).head
      val sortedArgs = Seq(variableArg, valueArg).sortBy(_.tokenInterval)
      val newTokenInterval = Interval(sortedArgs.head.tokenInterval.start, sortedArgs.last.tokenInterval.end)
      new RelationMention(
        labels = m.labels,
        tokenInterval = newTokenInterval,
        arguments = Map(VARIABLE -> Seq(variableArg), VALUE -> Seq(valueArg)),
        paths = m.paths,
        sentence = m.sentence,
        document = m.document,
        keep = m.keep,
        foundBy = m.foundBy,
        attachments = Set.empty
      )
    }
  }
  def yieldAmountActionFlow(mentions: Seq[Mention]): Seq[Mention] = {
    appropriateMeasurement(splitIntoBinary(mentions).filter(m => allowableTokenDistanceBetweenVarAndValue(m, 16)))
  }
  def fertilizerQuantityActionFlow(mentions: Seq[Mention]): Seq[Mention] = {
    appropriateMeasurement(splitIntoBinary(mentions).filter(m => allowableTokenDistanceBetweenVarAndValue(m, 12)))
  }
  def allowableTokenDistanceBetweenVarAndValue(mention: Mention, maxDist: Int): Boolean = {
    val variable = mention.arguments(VARIABLE).head
    val value = mention.arguments(VALUE).head
    val sorted = Seq(variable, value).sortBy(_.tokenInterval)
    sorted.last.start - sorted.head.end <= maxDist
  }
  def splitIntoBinary(mentions: Seq[Mention]): Seq[Mention] = {

    val (targets, other) = mentions.partition(m => {
      val valueLabels = m.arguments(VALUE).map(_.label)
      valueLabels.length > 1 &&
      valueLabels.distinct.length == 1
      }
    )
    val splitTargets = for {
      m <- targets
      value <- m.arguments(VALUE)
      newArgs = Map(VARIABLE -> m.arguments(VARIABLE), VALUE -> Seq(value))
    } yield copyWithArgs(m, newArgs, m.foundBy + "++splitIntoBinary")
    splitTargets ++ other
  }
  val labelToAppropriateUnits = Map(
    "Quantity" -> Set("t", "t/ha", "kg/ha", "kg", "d", "cm", "mg/l", "kg n ha-1"),
    "AreaSizeValue" -> Set("ha", "m2"),
    "YieldAmount" -> Set("t/ha", "kg/ha", "kg", "kg ha1"),
    "YieldIncrease" -> Set("t/ha", "kg/ha", "kg"),
    "FertilizerQuantity" -> Set("t", "kg/ha", "mg/l", "kg n ha-1")
  )

  def hasLetters(string: String): Boolean = {
    string.exists(ch => ch.isLetter || ch == '/')
  }
  def getNormString(m: Mention): String = {
    m.norms.get.head.split("\\s").filter(hasLetters).mkString(" ") // e.g., get "m2" from "6 m2"
  }
  def measurementIsAppropriate(m: Mention): Boolean = {
    // check mention type
    val probableUnit = m match {
      case tbm: TextBoundMention => getNormString(tbm)
      case rm: RelationMention => getNormString(rm.arguments(VALUE).head)
      case em: EventMention => getNormString(em.arguments(VALUE).head)
      case _ => throw new RuntimeException(s"Unknown mention type ${m.getClass}")
    }
    // check if any of the possible units shows up in the norm
    labelToAppropriateUnits
      .getOrElse(m.label, throw new RuntimeException(s"Unknown measurement label ${m.label}"))(probableUnit)
  }
  def appropriateMeasurement(mentions: Seq[Mention]): Seq[Mention] = {
    // applies to individual rules to check if the measurement is appropriate for the mention label
    mentions.filter(measurementIsAppropriate)
  }
  def makeEventFromUnitSplitByFertilizer(m: Mention): Mention = {
    val fertilizer = m.arguments("fertilizer").head.asInstanceOf[TextBoundMention].copy(labels = "Fertilizer" +: m.labels.tail)
    val value = m.arguments("number").map(_.text)
    val unit1 = m.arguments("unit1").map(_.text)
    val unit2 = m.arguments("unit2").head.text.replace("-1", "")
    val unit = unit1.mkString("") + "/" + unit2
    val newMention = new MeasurementMention(
      m.labels,
      m.tokenInterval,
      m.sentence,
      m.document,
      m.keep,
      m.foundBy,
      m.attachments,
      Some(value),
      Some(Seq(unit)),
      false
    )
    val newArgs = Map(VARIABLE -> Seq(fertilizer), VALUE -> Seq(newMention))
    val newEvent = new RelationMention(
      labels = Seq("FertilizerQuantity", "Event"),
      tokenInterval = m.tokenInterval,
      arguments = newArgs,
      paths = m.paths,
      sentence = m.sentence,
      document = m.document,
      keep = m.keep,
      foundBy = m.foundBy + "++makeQuantityFromUnitSplitByFertilizer",
      attachments = m.attachments
    )
    newEvent
  }

  def adjustQuantityNorm(mentions: Seq[Mention]): Seq[Mention] = {
    mentions.map { m =>
      // To be addressed later on handling decimal values.
      val value1 = m.arguments("value1").head.text.toFloat
      val value2 = m.arguments("value2").head.text.toFloat
      val unit = m.arguments("unit")
      val valueRange = f"${value1} -- ${value2}"
      new MeasurementMention(
        m.labels,
        m.tokenInterval,
        m.sentence,
        m.document,
        m.keep,
        m.foundBy,
        m.attachments,
        Some(Seq(valueRange)),
        Some(unit.map(_.text)),
        true
      )
    }
  }

  def makeEventFromSplitUnit(mentions: Seq[Mention]): Seq[Mention] = {
    // applies to individual rules to adjust norm
    mentions.map(makeEventFromUnitSplitByFertilizer)
  }

  def varietyToTBM(mentions: Seq[Mention]): Seq[Mention] = {
    mentions.flatMap(m =>
      // get all variety args
      m.arguments("variety")
        .map (am =>
          // make a standalone Crop mention from each variety arg
          am.asInstanceOf[TextBoundMention]
            .copy(
              labels = Seq("Crop", "Entity"),
              foundBy = m.foundBy + "++varietyToTBM"
              )
            )
          )
  }

  def copyWithFoundBy(mention: Mention, foundBy: String): Mention = {
    mention match {
      case tbm: TextBoundMention => tbm.copy(foundBy = foundBy)
      case rm: RelationMention => rm.copy(foundBy = foundBy)
      case em: EventMention => em.copy(foundBy = foundBy)
      case _ => throw new RuntimeException(s"Unknown mention type ${mention.getClass}")
    }
  }
}
