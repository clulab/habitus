package org.clulab.habitus.beliefs

import ai.lum.common.ConfigUtils._
import com.typesafe.config.{Config, ConfigBeanFactory}
import org.clulab.habitus.HabitusReader

object BeliefReader extends HabitusReader {
  val localConfig: Config = config[Config]("BeliefReader")
  val processor = BeliefProcessor()

  run(processor, inputDir, outputDir, threads)
}