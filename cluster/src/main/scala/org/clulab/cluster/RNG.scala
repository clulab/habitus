package org.clulab.cluster

class RNG(protected var seed: Int) {
  // This modulus guarantees that the remainder will be a positive Int.
  val modulus = 0x80000000L
  val multiplier = 1664525L
  val increment = 1013904223L

  def nextInt: Int = {
    val partial = multiplier * seed + increment
    if (partial < 0)
      println("How?")
    val nextInt = (partial % modulus).toInt

    seed = nextInt
    nextInt
  }

  def nextIntBelow(limit: Int): Int = {
    nextInt % limit
  }

  def random: Double = {
    val nextDouble = nextInt.toDouble / modulus

    nextDouble
  }

  def choice[T](seq: Seq[T]): T = {
    seq(nextIntBelow(seq.length))
  }

  def sample[T](seq: Seq[T], k: Int): Seq[T] = {

    def loop(seq: Seq[T], k: Int, result: List[T]): Seq[T] = {
      if (k == 0) result.reverse
      else {
        val index = nextIntBelow(seq.length)
        val value = seq(index)

        loop(seq.patch(index, Seq.empty, 1) , k - 1, value :: result)
      }
    }

    loop(seq, scala.math.min(k, seq.length), List.empty[T])
  }
}
