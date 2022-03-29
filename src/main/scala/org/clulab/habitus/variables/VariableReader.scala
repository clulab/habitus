package org.clulab.habitus.variables

import ai.lum.common.ConfigUtils._
import com.typesafe.config.{Config, ConfigBeanFactory}
import org.clulab.habitus.HabitusReader
import org.clulab.habitus.printer.PrintVariables

object VariableReader extends HabitusReader {
  val localConfig: Config = config[Config]("VarDatesReader")
  val masterResource: String = localConfig[String]("masterResource")
  val printVariables = ConfigBeanFactory.create(localConfig.getConfig("printVariables"), classOf[PrintVariables])
  val processor = VariableProcessor(masterResource)

  run(processor, inputDir, outputDir, threads, printVariables)
}
