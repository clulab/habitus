package org.clulab.habitus.variables

import ai.lum.common.ConfigUtils._
import com.typesafe.config.Config
import org.clulab.habitus.HabitusReader

object VariableReader extends HabitusReader {
  val localConfig: Config = config[Config]("VarReader")
  val masterResource: String = localConfig[String]("masterResource")
  val processor = VariableProcessor(masterResource)

  run(processor, inputDir, outputDir, threads)
}
