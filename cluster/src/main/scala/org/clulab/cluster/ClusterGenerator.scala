package org.clulab.cluster

import org.clulab.cluster.Types.{Clusters, Documents, LabelGroups}
import org.clulab.cluster.math.Maths

case class LabelsLengthMatrix(labels: LabelGroups, length: Int, matrix: Maths.MathMatrix)

abstract class ClusterGenerator(val corpus: Corpus, val linguist: Linguist) {

  def generate(documents: Documents, k: Int, frozenDocumentClussters: Clusters = emptyClusters,
      seededDocumentClusters: Clusters = emptyClusters, frozenDocuments: Documents = emptyDocuments):
      LabelsLengthMatrix
}
