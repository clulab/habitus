package org.clulab.cluster

import org.clulab.cluster.math.Maths.Math

class TestSoftKMeans extends Test {
  val tolerance = 1.0E-7
  val linguist = new Linguist()
  val corpus = new Corpus(linguist, null)
  val softKMeans = new SoftKMeans2(corpus, linguist)

  behavior of "SoftKMeans"

  it should "getComposite" in {
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val cluster = Array(
      documents(0),
      documents(1),
      documents(2),
      documents(3),
      documents(4)
    )
    val expectedVector = Array(3.0, 3.1, 3.2)
    val actualVector = Math.vectorRowToArray(softKMeans.getComposite(cluster))

    actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
      actualValue should be (expectedValue +- tolerance)
    }
  }

  it should "calculateCoefficient" in {
    val vector = Math.vectorInit(Array(1.0, 1.1, 1.2))
    val centroids = Math.matrixInit(Array(
      Array(3.5, 3.6, 3.7),
      Array(3.0, 3.1, 3.2)
    ))
    val expectedVector = Array(0.01139781, 0.98860219)
    val actualVector = Math.vectorRowToArray(softKMeans.calculateCoefficient(vector, centroids))

    actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
      actualValue should be (expectedValue +- tolerance)
    }
  }

  it should "updateSoftCentroids" in {
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val docVecs = Math.matrixInit(documents.map(_.vector))
    val npMatrix = Math.matrixInit(Array(
      Array(1.13978073e-002, 9.88602193e-001),
      Array(3.00638249e-004, 9.99699362e-001),
      Array(1.00183521e-254, 1.00000000e+000),
      Array(9.99999046e-001, 9.53673407e-007),
      Array(9.96838813e-001, 3.16118714e-003)
    ))
    val expectedMatrix = Array(
      Array(4.47897771, 4.57897771, 4.67897771),
      Array(2.00698096, 2.10698096, 2.20698096)
    )
    val actualMatrix = Math.matrixToArray(softKMeans.updateSoftCentroids(docVecs, npMatrix))

    actualMatrix.zip(expectedMatrix).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be (expectedValue +- tolerance)
      }
    }
  }

  it should "checkConvergence" in {
    val centroids = Math.matrixInit(Array(
      Array(4.47897771, 4.57897771, 4.67897771),
      Array(2.00698096, 2.10698096, 2.20698096)
    ))
    val lastCentroids = Math.matrixInit(Array(
      Array(3.5, 3.6, 3.7),
      Array(3.0, 3.1, 3.2)
    ))
    val j = 1
    val expectedResult = false
    val actualResult = softKMeans.checkConvergence(centroids, lastCentroids, j)

    actualResult should be (expectedResult)
  }

  it should "calculateMatrix" in {
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val npDocToSeededK = Math.matrixInit(Array(
      Array(0d, 0d),
      Array(0d, 0d),
      Array(0d, 0d),
      Array(0d, 0d),
      Array(0d, 0d)
    ))
    val npCentroids = Math.matrixInit(Array(
      Array(3.5, 3.6, 3.7),
      Array(3.0, 3.1, 3.2)
    ))
    val npDocVecs = Math.matrixInit(documents.map(_.vector))
    val documentSeedCounts = Array(0, 0, 0, 0, 0)
    val expectedMatrix = Array(
      Array(0.01139781, 0.98860219),
      Array(3.00638249e-04, 9.99699362e-01),
      Array(1.00183521e-254, 1.00000000e+000),
      Array(9.99999046e-01, 9.53673407e-07),
      Array(0.99683881, 0.00316119)
    )
    val actualMatrix = Math.matrixToArray(softKMeans.calculateMatrix(npDocToSeededK, npCentroids, npDocVecs, documentSeedCounts))

    actualMatrix.zip(expectedMatrix).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be(expectedValue +- tolerance)
      }
    }
  }

  it should "runSoftClustering" in {
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val npDocToSeededK = Math.matrixInit(Array(
      Array(0d, 0d),
      Array(0d, 0d),
      Array(0d, 0d),
      Array(0d, 0d),
      Array(0d, 0d)
    ))
    val seededAndGeneratedClusters = Array(
      Array(documents(3), documents(2)),
      Array(documents(4), documents(0))
    )
    val npDocVecs = Math.matrixInit(documents.map(_.vector))
    val documentSeedCounts = Array(0, 0, 0, 0, 0)
    val expectedMatrix = Array(
      Array(1.31318669e-11, 1.00000000e+00),
      Array(1.27373969e-88, 1.00000000e+00),
      Array(3.02155569e-04, 9.99697844e-01),
      Array(1.00000000e+00, 9.00379662e-13),
      Array(1.00000000e+00, 2.75815469e-16)
    )
    val actualMatrix = Math.matrixToArray(softKMeans.runSoftClustering(npDocToSeededK, seededAndGeneratedClusters, npDocVecs, documentSeedCounts))

    actualMatrix.zip(expectedMatrix).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be (expectedValue +- tolerance)
      }
    }
  }

  it should "assignSoftLabels" in {
    val npDocToSeededK = Math.matrixInit(Array(
      Array(0d, 0d),
      Array(0d, 0d),
      Array(0d, 0d),
      Array(0d, 0d),
      Array(0d, 0d)
    ))
    val npMatrix = Math.matrixInit(Array(
      Array(1.31318669e-11, 1.00000000e+00),
      Array(1.27373969e-88, 1.00000000e+00),
      Array(3.02155569e-04, 9.99697844e-01),
      Array(1.00000000e+00, 9.00379662e-13),
      Array(1.00000000e+00, 2.75815469e-16)
    ))
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val expectedMatrix = Array(
      Array(documents(3), documents(4)),
      Array(documents(0), documents(1), documents(2))
    )
    val actualMatrix = softKMeans.assignSoftLabels(npDocToSeededK, npMatrix, documents)

    actualMatrix.zip(expectedMatrix).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be (expectedValue)
      }
    }
  }

  it should "cScore" in {
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val clusters = Array(
      Array(documents(3), documents(4)),
      Array(documents(0), documents(1), documents(2))
    )
    val metaCentroid = Math.vectorInit(Array(3.0, 3.1, 3.2))
    val expectedResult = 0.16253260243764986
    val actualResult = softKMeans.clusterScore(clusters, documents.length, metaCentroid)

    expectedResult should be (actualResult +- tolerance)
  }

  it should "generateClustersRandomPair" in {
    val k = 2
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val expectedMatrix = Array(
      Array(documents(3), documents(2)),
      Array(documents(4), documents(0))
    )
    val actualMatrix = softKMeans.generateClustersRandomPair(k, documents)

    actualMatrix.zip(expectedMatrix).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be (expectedValue)
      }
    }
  }

  it should "innerGenerate" in {
    val k = 2
    val kSeeded = 0
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val npDocuments = documents
    val npDocToSeeded = Math.emptyMatrix
    val frozenDocumentClusters = emptyClusters
    val npDocVecs = Math.matrixInit(documents.map(_.vector))
    val documentSeedCounts = Array(0, 0, 0, 0, 0)
    val seededClusters = emptyClusters
    val allDocuments = documents
    val metaCentroid = Math.vectorInit(Array(3.0, 3.1, 3.2))
    val expectedClustersScore = ClustersScore(
      Array(
        Array(documents(0), documents(1)),
        Array(documents(3), documents(4))
      ),
      7.336354063280828,
      Math.matrixInit(Array(
        Array(1.00000000e+00, 9.09494702e-13),
        Array(1.00000000e+00, 1.48643628e-21),
        Array(5.00000000e-01, 5.00000000e-01),
        Array(1.48643628e-21, 1.00000000e+00),
        Array(9.09494702e-13, 1.00000000e+00)
      ))
    )
    val actualClustersScore = softKMeans.innerGenerate(k, kSeeded, documents, npDocuments, npDocToSeeded, frozenDocumentClusters,
        npDocVecs, documentSeedCounts, seededClusters, allDocuments, metaCentroid).get

    actualClustersScore.clusters.zip(expectedClustersScore.clusters).foreach { case (actualCluster, expectedCluster) =>
      actualCluster.zip(expectedCluster).foreach { case (actualDocument, expectedDocument) =>
        actualDocument should be (expectedDocument)
      }
    }
    actualClustersScore.score should be (expectedClustersScore.score +- tolerance)
    Math.matrixToArray(actualClustersScore.matrix).zip(Math.matrixToArray(expectedClustersScore.matrix)).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be (expectedValue +- tolerance)
      }
    }
  }

  it should "getKRange" in {
    val k = 2
    val documentCount = 5
    val seededClusterCount = 0
    val expectedResult = Range(2, 3)
    val actualResult = softKMeans.getKRange(k, documentCount, seededClusterCount)

    expectedResult should be (actualResult)
  }

  it should "seedClusters" in {
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val seededClusters = Array(
      Array(documents(1), documents(3))
    )
    val documentSeedCounts = Array(0, 1, 0, 1, 0)
    val expectedMatrix = Array(
      Array(0d, 1d, 0d, 1d, 0d)
    )
    val actualMatrix = Math.matrixToArray(softKMeans.seedClusters(seededClusters, documents, documentSeedCounts))

    actualMatrix.zip(expectedMatrix).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be (expectedValue +- tolerance)
      }
    }
  }

  it should "getLabelList" in {
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val clusters = Array(
      Array(documents(0), documents(1)),
      Array(documents(3), documents(4))
    )
    val expectedMatrix = Array(
      Array(0),
      Array(0),
      Array.empty,
      Array(1),
      Array(1)
    )
    val actualMatrix = softKMeans.getLabelGroups(clusters, documents)

    actualMatrix.zip(expectedMatrix).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be (expectedValue)
      }
    }
  }

  it should "generate" in {
    // Make a new one for the sake of RNG.
    val softKMeans = new SoftKMeans2(corpus, linguist)
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val expectedLabels = Array(
      Array(0),
      Array(0),
      Array.empty,
      Array(1),
      Array(1)
    )
    val expectedK = 2
    val expectedMatrix = Array(
      Array(1.00000000e+00, 9.09494702e-13),
      Array(1.00000000e+00, 1.48643628e-21),
      Array(5.00000000e-01, 5.00000000e-01),
      Array(1.48643628e-21, 1.00000000e+00),
      Array(9.09494702e-13, 1.00000000e+00)
    )
    val LabelsLengthMatrix(actualLabels, actualK, actualMatrix) = softKMeans.generate(documents, 2)

    actualLabels.zip(expectedLabels).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be (expectedValue)
      }
    }
    actualK should be (expectedK)
    Math.matrixToArray(actualMatrix).zip(expectedMatrix).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be (expectedValue +- tolerance)
      }
    }
  }

  it should "generate seeded" in {
    // Make a new one for the sake of RNG.
    val softKMeans = new SoftKMeans2(corpus, linguist)
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val seededClusters = Array(
      Array(documents(1), documents(3))
    )
    val expectedLabels = Array(
      Array(1),
      Array(0),
      Array(0),
      Array(0),
      Array(0)
    )
    val expectedK = 2
    val expectedMatrix = Array(
      Array(4.96918279e-167, 1.00000000e+000),
      Array(1.00000000e+000, 0.00000000e+000),
      Array(1.00000000e+000, 9.09494771e-013),
      Array(1.00000000e+000, 0.00000000e+000),
      Array(9.99999997e-001, 3.02430360e-009)
    )
    val LabelsLengthMatrix(actualLabels, actualK, actualMatrix) = softKMeans.generate(documents, 2, seededClusters = seededClusters)

    actualLabels.zip(expectedLabels).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be(expectedValue)
      }
    }
    actualK should be(expectedK)
    Math.matrixToArray(actualMatrix).zip(expectedMatrix).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be(expectedValue +- tolerance)
      }
    }
  }

  it should "run soft clustering" in {
    // Make a new one for the sake of RNG.
    val softKMeans = new SoftKMeans2(corpus, linguist)
    val documents = Array(
      newDocument(0, "doc0", Array(1.0, 1.1, 1.2)),
      newDocument(1, "doc1", Array(2.0, 2.1, 2.2)),
      newDocument(2, "doc2", Array(3.0, 3.1, 3.2)),
      newDocument(3, "doc3", Array(4.0, 4.1, 4.2)),
      newDocument(4, "doc4", Array(5.0, 5.1, 5.2))
    )
    val docToSeededK = Math.matrixInit(Array(
      emptyVectorOfDouble,
      emptyVectorOfDouble,
      emptyVectorOfDouble,
      emptyVectorOfDouble,
      emptyVectorOfDouble
    ))
    val clusters = Array(
      Array(documents(0), documents(1)),
      Array(documents(2), documents(3), documents(4))
    )
    val npDocuments = Math.matrixInit(documents.map(_.vector))
    val documentSeededCounts = Array(
      0, 0, 0, 0, 0
    )
    val expectedMatrix = Array(
      Array(1.00000000e+00, 2.75815402e-16),
      Array(1.00000000e+00, 9.00379925e-13),
      Array(3.02155525e-04, 9.99697844e-01),
      Array(1.27300093e-88, 1.00000000e+00),
      Array(1.31318672e-11, 1.00000000e+00)
    )
    val actualMatrix = softKMeans.runSoftClustering(docToSeededK, clusters, npDocuments, documentSeededCounts)

    Math.matrixToArray(actualMatrix).zip(expectedMatrix).foreach { case (actualVector, expectedVector) =>
      actualVector.zip(expectedVector).foreach { case (actualValue, expectedValue) =>
        actualValue should be(expectedValue +- tolerance)
      }
    }
  }
}
