package org.clulab.habitus.variables

import ai.lum.common.ConfigUtils._
import com.typesafe.config.{Config, ConfigBeanFactory}
import org.clulab.habitus.HabitusReader

object PlantingAreaReader extends HabitusReader {
  val localConfig: Config = config[Config]("PlantingAreaReader")
  val masterResource: String = localConfig[String]("masterResource")
  val processor = VariableProcessor(masterResource)

  run(processor, inputDir, outputDir, threads)
}
