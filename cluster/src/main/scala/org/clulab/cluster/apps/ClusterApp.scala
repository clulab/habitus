package org.clulab.cluster.apps

import scala.util.Random
import org.clulab.cluster.{Document, LabelsLengthMatrix, RNG, SoftKMeans2}
import org.clulab.cluster.Types.{Clusters, Documents, MatrixOfDouble, MatrixOfInt}
import org.clulab.cluster.math.Maths.Math

object ClusterApp extends App {
  val clusterCount = 5
  val vectorSize = 300
  val documentCount = 10

  def newDocument(rndgen: RNG, index: Int): Document = {
    val text = s"doc$index"
    val vector = Math.vectorInit(Array.fill(vectorSize)(rndgen.random))
    val document = new Document(index, text, text, text.split(' '), "", "")

    document.setVector(vector)
    document
  }

  def testSoftClustering(documents: Documents, seed: Int): LabelsLengthMatrix = {
    val softKMeans = new SoftKMeans2(null, null, seed)
    val labels_k_tuple = softKMeans.generate(documents, clusterCount)

    labels_k_tuple
  }

  def testClustering(round: Int, n: Int): Float = {
    val seed = round + n
    val rndgen = new RNG(seed) // new Random(seed)
    val documents = Array.tabulate(n) { index => newDocument(rndgen, index) }
    val start = System.currentTimeMillis
    val labels_k_tuple = testSoftClustering(documents, seed)
    val stop = System.currentTimeMillis
    val elapsed = (stop - start).toFloat / 1000

    elapsed
  }

  def run(): Unit = {
    val sizes = Array(10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000)

    Range(0, 1).foreach { round =>
      sizes.foreach { size =>
        val elapsed = testClustering(round, size)

        println(s"$round\t$size\t$elapsed")
      }
    }
  }

  println("Keith was here!")
  run()
}
