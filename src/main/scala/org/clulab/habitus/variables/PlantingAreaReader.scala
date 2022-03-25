package org.clulab.habitus.variables

import ai.lum.common.ConfigUtils._
import com.typesafe.config.{Config, ConfigBeanFactory, ConfigFactory}
import org.clulab.habitus.{GenericProcessor, HabitusReader}
import org.clulab.habitus.utils._
import org.clulab.odin.Mention
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}

import java.io.File
import scala.beans.BeanProperty

object PlantingAreaReader extends HabitusReader {

//  case class PrintVariables(@BeanProperty var mentionLabel: String, @BeanProperty var mentionType: String, @BeanProperty var mentionExtractor: String) {
//    def this() = this("", "", "")
//  }

  val localConfig: Config = config[Config]("PlantingAreaReader")
  val masterResource: String = localConfig[String]("masterResource")
//  val label: String = config[String]("PlantingAreaReader.label")
//  val mentionArgs: List[String] = config[List[String]]("PlantingAreaReader.args")
//  val printVariables = PrintVariables(label, mentionArgs.head, mentionArgs.last)
  val printVariables = ConfigBeanFactory.create(localConfig.getConfig("printVariables"), classOf[PrintVariables])
  val processor = VariableProcessor(masterResource)
  run(processor, inputDir, outputDir, threads, masterResource, printVariables)


}
