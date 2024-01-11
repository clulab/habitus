package org.clulab.cluster

class TestRNG extends Test {

  behavior of "RNG"

  it should "nextInt" in {
    val seed = 42
    val rng = new RNG(seed)
    val expectedValues = Array(1083814273, 378494188, 331920219, 955863294, 1613448261)
    val actualValues = expectedValues.map { _ => rng.nextInt }

    actualValues should equal (expectedValues)
  }

  it should "nextIntBelow" in {
    val seed = 42
    val rng = new RNG(seed)
    val expectedValues = Array(3, 8, 9, 4, 1, 2, 5, 2, 1, 6)
    val actualValues = expectedValues.map { _ => rng.nextIntBelow(10) }

    actualValues should equal (expectedValues)
  }

  it should "random" in {
    val seed = 42
    val rng = new RNG(seed)
    val expectedValues = Array(5046, 1762, 1545, 4451, 7513, 513, 8945, 2369, 7476, 9892)
    val actualValues = expectedValues.map { _ => (rng.random * 10000).toInt }

    actualValues should equal(expectedValues)
  }

  it should "choice" in {
    val seed = 42
    val rng = new RNG(seed)
    val population = Array(0, 1, 2, 3, 4)
    val expectedValues = Array(3, 3, 4, 4, 1, 2, 0, 2, 1, 1)
    val actualValues = expectedValues.map { _ => rng.choice(population) }

    actualValues should equal(expectedValues)
  }

  it should "sample" in {
    val seed = 42
    val rng = new RNG(seed)
    val population = Array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    val expectedValues = Array(3, 8, 4, 1, 6, 5, 9, 2, 7, 0)
    val actualValues = rng.sample(population, 10)

    actualValues should equal(expectedValues)
  }
}
