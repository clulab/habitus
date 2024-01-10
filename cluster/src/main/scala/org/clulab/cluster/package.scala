package org.clulab

import org.clulab.cluster.Types.{Cluster, Clusters, Documents}

package object cluster {
  // TODO: What can I use here instead of Array?

  val emptyCluster = Array.empty[Document]
  val emptyClusters = Array.empty[Cluster]

  val emptyLabelGroup = Array.empty[Int]
  val emptyLabelGroups = Array.empty[Array[Int]]

  val emptyDocuments = Array.empty[Document]

  val emptyEmbedding = Array.empty[Double]

  val emptyEmbeddings = Array.empty[Array[Double]]

  val emptyVectorOfInt = Array.empty[Int]
  val emptyVectorOfDouble = Array.empty[Double]

  val emptyMatrixOfDouble = Array.empty[Array[Double]]
}
