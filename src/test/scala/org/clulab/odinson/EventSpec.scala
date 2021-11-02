package org.clulab.odinson

import org.scalatest._
import ai.lum.odinson._

class EventSpec extends FlatSpec with Matchers {

  def testEventTrigger(m: OdinsonMatch, start: Int, end: Int): Unit = {
    val trigger = m match {
      case e: EventMatch => e.trigger
      case s: StateMatch => s
      case _ => ???
    }
    trigger.start shouldEqual start
    trigger.end shouldEqual end
  }

  def testEventArguments(m: OdinsonMatch, desiredArgs: Seq[Argument]): Unit = {
    // extract match arguments from the mathing objects
    val matchArgs = for(nc <- m.namedCaptures)
      yield Argument(nc.name, nc.capturedMatch.start, nc.capturedMatch.end)
      // all desired args should be there, in the right number
      val groupedMatched = matchArgs.groupBy(_.name)
      val groupedDesired = desiredArgs.groupBy(_.name)
      //
      for ((desiredRole, desired) <- groupedDesired) {
        // there should be arg(s) of the desired label
        groupedMatched.keySet should contain (desiredRole)
        // should have the same number of arguments of that label
        val matchedForThisRole = groupedMatched(desiredRole)
        desired should have size matchedForThisRole.size
        for (d <- desired) {
          matchedForThisRole should contain (d)
        }
        // there shouldn't be any found arguments that we didn't want
        val unwantedArgs = groupedMatched.keySet.diff(groupedDesired.keySet)
        unwantedArgs shouldBe empty
      }
  }

  def createArgument(name: String, start: Int, end: Int): Argument = Argument(name, start, end)

}

case class Argument(name: String, start: Int, end: Int) {
  override def toString: String = {
    s"Argument(name=$name, start=$start, end=$end)"
  }
}
