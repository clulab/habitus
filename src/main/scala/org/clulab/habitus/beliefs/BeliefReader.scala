package org.clulab.habitus.beliefs

import ai.lum.common.ConfigUtils._
import com.typesafe.config.{Config, ConfigBeanFactory}
import org.clulab.habitus.HabitusReader
import org.clulab.habitus.printer.PrintVariables

object BeliefReader extends HabitusReader {
  val localConfig: Config = config[Config]("BeliefReader")
  val printVariables = ConfigBeanFactory.create(localConfig.getConfig("printVariables"), classOf[PrintVariables])
  val processor = BeliefProcessor()

  run(processor, inputDir, outputDir, threads, printVariables)
}