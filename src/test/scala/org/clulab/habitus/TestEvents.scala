package org.clulab.habitus

import org.scalatest._
import ai.lum.odinson.BaseSpec

class TestEvents extends EventSpec {

  def doc = TestUtils.getDocument("chopsticks-spoon")
  def ee = TestUtils.mkExtractorEngine(doc)

  "Odinson" should "find two events with one tool each" in {
    val pattern = """
      trigger = [lemma=eat]
      theme: ^food = >dobj
      tool: ^tool = >nmod_with >conj?
    """
    val q = ee.compiler.compileEventQuery(pattern)
    val results = ee.query(q, 5)
    results.totalHits should equal (1)
    results.scoreDocs.head.matches should have size 2
    val Array(m1, m2) = results.scoreDocs.head.matches
    // test trigger
    testEventTrigger(m1, start = 1, end = 2)
    testEventTrigger(m2, start = 1, end = 2)
    // test arguments
    val desiredArgs1 = Seq(createArgument("theme", 2, 3), createArgument("tool", 4, 5))
    val desiredArgs2 = Seq(createArgument("theme", 2, 3), createArgument("tool", 7, 8))
    testEventArguments(m1, desiredArgs1)
    testEventArguments(m2, desiredArgs2)
    ee.clearState()
  }

  it should "find one events with two tools" in {
    val pattern = """
      trigger = [lemma=eat]
      theme: ^food = >dobj
      tool: ^tool+ = >nmod_with >conj?
    """
    val q = ee.compiler.compileEventQuery(pattern)
    val results = ee.query(q, 5)
    results.totalHits should equal (1)
    results.scoreDocs.head.matches should have size 1
    val Array(m1) = results.scoreDocs.head.matches
    // test trigger
    testEventTrigger(m1, start = 1, end = 2)
    // test arguments
    val desiredArgs1 = Seq(createArgument("theme", 2, 3), createArgument("tool", 4, 5), createArgument("tool", 7, 8))
    testEventArguments(m1, desiredArgs1)
    ee.clearState()
  }

}
