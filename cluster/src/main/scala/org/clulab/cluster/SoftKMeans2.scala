package org.clulab.cluster

import org.clulab.cluster.Types.{Cluster, Clusters, Documents, Embedding, Embeddings, LabelGroups, MatrixOfInt, VectorOfInt}
import org.clulab.cluster.math.Maths.{Math, MathColVector, MathMatrix, MathRowVector}

import scala.concurrent.ExecutionContext

case class ClustersScore(clusters: Clusters, score: Double, matrix: MathMatrix)

class SoftKMeans2(corpus: Corpus, linguist: Linguist, seed: Int = 0) extends ClusterGenerator(corpus, linguist) {
  implicit val executionContext = ExecutionContext.global

  val beta = 1.1
  val exponent = 2d / (beta - 1)
  val threshold = 0.6
  val loopCount = 10
  val updateCount = 100
  val updateLimit = 0.00001
  val perturbance = 0.0000000000001
  val rndgen = new RNG(seed)
  val dimension = 300
  val emptyLabelsLengthMatrix = LabelsLengthMatrix(emptyLabelGroups, 0, Math.emptyMatrix)
  val loopArray = new Array[Int](loopCount)

  // protected
  def generateClustersRandomPair(k: Int, documents: Documents): Clusters = {
    val sample = rndgen.sample(documents, k * 2).toArray
    val pairedSample = sample.sliding(2, 2).toArray

    pairedSample
  }

  // protected
  // For each row (document) find the column (cluster) in which seededMatrix value exceeds threshold.
  def assignSoftLabels(npDocToSeededK: MathMatrix, npMatrix: MathMatrix, npDocuments: Documents): Clusters = {
    val seededMatrix = Math.matrixAdd(npMatrix, npDocToSeededK)
    val clusters = Range(0, Math.matrixColCount(seededMatrix)).toArray.map { clusterIndex =>
      val seededVectorsAndNpDocuments = Math.rowIterator(seededMatrix).zip(npDocuments.iterator)

      seededVectorsAndNpDocuments.flatMap { case (seededVector, npDocument) =>
        if (threshold <= Math.vectorRowGet(seededVector, clusterIndex)) Some(npDocument)
        else None
      }.toArray
    }

    clusters
  }

  // protected
  def getLabelGroups(clusters: Clusters, documents: Documents): LabelGroups = {
    val clustersIndices = clusters.indices.toArray
    // Calculate indexes of the clusters that each document is in.
    // Can a document ever be in more than one cluster?  No clusters?
    val labelGroups = documents.map { document =>
      clustersIndices.filter { index =>
        clusters(index).contains(document)
      }
    }

    labelGroups
  }

  def getComposite(cluster: Cluster): MathRowVector = {
    if (cluster.isEmpty)
      Math.emptyRowVector
    else {
      val matrix = Math.matrixInit(cluster.map(_.vector))
      val composite = Math.matrixColAve(matrix)

      composite
    }
  }

  def clusterScore(clusters: Clusters, documentCount: Int, metaCentroid: MathRowVector): Double = {
    val centroids = clusters.map(getComposite)
    val (n, k) = (documentCount, clusters.length)

    def bAndW(): (Double, Double) = {
      var betweennessSum = 0d
      var withinnessSum = 0d
      clusters.zip(centroids).foreach { case (cluster, centroid) =>
        val clusterLen = cluster.length
        if (clusterLen > 0) {
          val betweennessDist = Math.cosineDistance(centroid, metaCentroid)
          betweennessSum += clusterLen * (betweennessDist * betweennessDist)
          cluster.foreach { document =>
            val withinnessDist = Math.cosineDistance(document.vector, centroid)
            withinnessSum += withinnessDist * withinnessDist
          }
        }
      }
      (betweennessSum, withinnessSum)
    }

    if (k > 1) {
      if (n == k) 0d // Avoid calculation if it is not necessary.
      else {
        val (b, w) = bAndW()

        if (w != 0.0)
          (b * (n - k)) / (w * (k - 1d))
        else
          Float.NaN
      }
    }
    else
      Float.NaN
  }

  // protected
  def innerGenerate(k: Int, kSeeded: Int, documents: Documents, npDocuments: Documents,
      docToSeeded: MathMatrix, frozenDocumentClusters: Clusters, npDocVecs: MathMatrix, documentSeedCounts: VectorOfInt,
      seededClusters: Clusters, allDocuments: Documents, metaCentroid: Embedding): Option[ClustersScore] = {
    // Expand docToSeeded to be npDocuments.length * k using 0 to pad.
    val npDocToSeededK = Math.matrixInit(npDocuments.length, k) { (documentIndex, kIndex) =>
      Math.matrixGet(docToSeeded, documentIndex, kIndex, 0d)
    }

    def loop(generatedClusters: Clusters): ClustersScore = {
      val seededAndGeneratedClusters = seededClusters ++ generatedClusters
      val npMatrix = runSoftClustering(npDocToSeededK, seededAndGeneratedClusters, npDocVecs, documentSeedCounts)
      val clusters = assignSoftLabels(npDocToSeededK, npMatrix, npDocuments)
      val allClusters = frozenDocumentClusters ++ clusters
      val score = clusterScore(allClusters, allDocuments.length, metaCentroid)

      ClustersScore(clusters, score, npMatrix)
    }

    // Clusters are generated serially for the sake of the RNG.
    val generatedClusters = loopArray.map { _ =>
      generateClustersRandomPair(k - kSeeded, documents)
    }
    val clustersScores = generatedClusters.par.map(loop)
    val validClustersScores = clustersScores.filterNot(_.score.isNaN)

    validClustersScores.headOption.map(_ => validClustersScores.maxBy(_.score))
  }

  // protected
  def seedClusters(seededClusters: Clusters, documents: Documents, documentSeedCounts: VectorOfInt): MathMatrix = {
    val npDocToSeeded = documents.zip(documentSeedCounts).map { case (document, count) =>
      val seed =
          if (count != 0) 1d / count
          else 0d
      val vector = seededClusters.map { seededCluster =>
        if (seededCluster.contains(document)) seed
        else 0d
      }

      vector
    }

    Math.matrixInit(npDocToSeeded)
  }

  // protected
  def getKRange(k: Int, documentCount: Int, seededClusterCount: Int): Range = {
    assert(1 <= documentCount)
    val kMin = scala.math.max(2, seededClusterCount)
    val kMax = scala.math.min(documentCount / 2, k)
    if (kMin <= kMax)
      Range.inclusive(kMin, kMax)
    else {
      assert(1 <= seededClusterCount)
      Range.inclusive(seededClusterCount, seededClusterCount)
    }
  }

  def generate(documents: Documents, k: Int, frozenClusters: Clusters = emptyClusters,
      seededClusters: Clusters = emptyClusters, frozenDocuments: Documents = emptyDocuments):
      LabelsLengthMatrix = {
    val kSeeded = seededClusters.length
    val kRange = getKRange(k, documents.length, kSeeded)
    val npDocuments = documents
    val npDocVecs = Math.matrixInit(documents.map(_.vector))
    val allDocuments = frozenDocuments ++ documents
    val metaCentroid = getComposite(allDocuments)
    val documentSeedCounts = documents.map { document =>
      seededClusters.count { seededCluster => seededCluster.contains(document) }
    }
    val docToSeeded = seedClusters(seededClusters, npDocuments, documentSeedCounts)
    val clustersScoreTuples = kRange.toArray.flatMap { k =>
      innerGenerate(k, kSeeded, documents, npDocuments, docToSeeded, frozenClusters, npDocVecs, documentSeedCounts,
          seededClusters, allDocuments, metaCentroid)
    }
    if (clustersScoreTuples.nonEmpty) {
      val bestClustersScoreTuples = clustersScoreTuples.maxBy(_.score)
      val bestClusters = bestClustersScoreTuples.clusters
      val labels = getLabelGroups(bestClusters, documents)
      val bestMatrix = bestClustersScoreTuples.matrix

      LabelsLengthMatrix(labels, bestClusters.length, bestMatrix)
    }
    else {
      println("What now?")
      emptyLabelsLengthMatrix
    }
  }

  // protected
  def updateSoftCentroids(npDocVecs: MathMatrix, npMatrix: MathMatrix): MathMatrix = {

    def calculateCentroid(clusterIndex: Int): MathRowVector = {
      val wk_vector = Math.matrixGetCol(npMatrix, clusterIndex)
      val wkSum = Math.vectorColSum(wk_vector)
      // This transposes at the same time.
      val product = Math.matrixInit(Math.matrixColCount(npDocVecs), Math.vectorRowCount(wk_vector)) { (dim1, dim2) =>
        val op1 = Math.vectorColGet(wk_vector, dim2)
        val op2 = Math.matrixGet(npDocVecs, dim2, dim1)

        op1 * op2
      }
      val centroid = Math.matrixMap(product)(Math.vectorRowSum(_) / wkSum)

      centroid
    }

    // Change this into matrixMapCols
    val centroids = Range(0, Math.matrixColCount(npMatrix)).toArray.map { clusterIndex =>
      calculateCentroid(clusterIndex)
    }

    Math.matrixInit(centroids)
  }

  // This can be protected later, or named with _ from Python
  def calculateCoefficient(vector: MathRowVector, centroids: MathMatrix): MathRowVector = {
    val sums = Math.matrixAddScalar(centroids, perturbance)
    val diffs = Math.matrixRowSub(vector, sums) // so vector - sum
    val norms = Math.matrixMap(diffs)(Math.vectorNorm)
    val reciprocals = Math.vectorRecip(norms)
    val powers = Math.vectorPow(reciprocals, exponent)
    val b = Math.vectorRowSum(powers)

    val as = Math.vectorPow(norms, exponent)
    val products = Math.vectorMul(as, b)
    val quotients = Math.vectorRecip(products)

    quotients
  }

  // protected
  // TODO: Look for Embeddintgs and convert to Matrix?
  def calculateMatrix(npDocToSeededK: MathMatrix, npCentroids: MathMatrix, npDocVecs: MathMatrix, documentSeedCounts: VectorOfInt): MathMatrix = {
    val matrix = Math.matrixMapWithIndex(npDocVecs) { case (docVec, documentIndex) =>
      if (documentSeedCounts(documentIndex) == 0)
        calculateCoefficient(docVec, npCentroids)
      else
        Math.matrixGetRow(npDocToSeededK, documentIndex)
    }

    matrix
  }

  def checkConvergence(centroids: MathMatrix, lastCentroids: MathMatrix, count: Int): Boolean = {
    count > updateCount || {
      val diffs = Math.matrixSub(centroids, lastCentroids)
      val aves = Math.matrixRowAve(diffs)

      Math.vectorMax(aves) < updateLimit // TODO, needs to be absolute value
    }
  }

  // protected
  def runSoftClustering(npDocToSeededK: MathMatrix, seededAndGeneratedClusters: Clusters, npDocVecs: MathMatrix, documentSeedCounts: VectorOfInt): MathMatrix = {
    val npCentroidRows = seededAndGeneratedClusters.map { cluster =>
      val matrix = Math.matrixInit(cluster.map(_.vector))

      Math.matrixColAve(matrix)
    }
    var npCentroids = Math.matrixInit(npCentroidRows)
    var (count, converged) = (0, false)

    while (true) {
      val npMatrix = calculateMatrix(npDocToSeededK, npCentroids, npDocVecs, documentSeedCounts)
      if (converged)
        return npMatrix
      val lastNpCentroids = npCentroids
      npCentroids = updateSoftCentroids(npDocVecs, npMatrix)
      count += 1
      converged = checkConvergence(npCentroids, lastNpCentroids, count)
    }
    null
  }
}
