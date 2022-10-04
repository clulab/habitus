package org.clulab.habitus.variables

import org.clulab.odin.TextBoundMention
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}

import java.io.{File, PrintWriter}

object FindNewVarieties {

  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToMap(args)
    val inputDir = "path/to/input/dir"
    val outputDir = "path/to/output/dir"
    val threads = props.get("threads").map(_.toInt).getOrElse(1)

    run(inputDir, outputDir, threads)
  }

  def run(inputDir: String, outputDir: String, threads: Int): Unit = {
    new File(outputDir).mkdirs()

    // fixme: temporary, simple text cleanup
    def cleanText(text: String): String = text
      .replace("\n", " ")
      .replace("- ", "")

    val processor = VariableProcessor()
    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files

    new PrintWriter(new File(outputDir + "/mentions.tsv")).autoClose { pw =>
      pw.println("filename\tmention type\tfound by\tsentence\tmention text\targs in all next columns (argType: argText)")
      for (file <- parFiles) {
        try {
          val unfiltered = FileUtils.getTextFromFile(file)
          val text = cleanText(unfiltered)
 // Sample text:
//          val text = "Mutants such as Huayu 22 and Fu 22. Leaf rust and late leaf spot resistance were successfully introgressed into the cultivated groundnut varieties (e.g. ICGV 91114, ICGS 76, ICGV 91278, JL 24, and DH 86) using two synthetic resistance sources.\nhigh oleic acid content and nematode resistant variety, 'Tifguard' was developed through the application of this technique.\nIntrogression of rust resistance from 'GPBD 4' groundnut cultivar into susceptible varieties ICGV 91114, JL 24 and TAG 24 were employed through.\nMutant variety Fu 22 is known for its tolerance against A. flavus.\nThe first high oleic groundnut variety released in the world was SunOleic 95R, which was derived from a cross between a high oleic breeding line F435 and a component line 'Sunrunner'.\nThe model was first calibrated and validated using experiments carried out in 1994, 1996 and 2000, on two groundnut varieties, Fleur 11 and GH 119-20, at the experimental station of Bambey in Senegal, using sprinkler irrigation methods.\nThe two groundnut varieties used were Fleur 11, a Spanish early variety, cultivated under rainfed conditions in the North of Senegal, and GH 119-20, a Virginia late variety with large shells, cultivated in the South of Senegal.\nThis study provides empirical evidence of factors that influence the adoption of an improved groundnut variety (La Fleur 11), and chemical fertilizer in the Senegalese Groundnut Basin.\nThe evolution of the total AGB is father for the early maturity variety (Fleur11) than the medium maturity variety (73-33) for all experiments and for all seasons.\nThis variety (GC8-35) had a specific adaptation to favourable environments.\nThe variety 78-936 gave also moderate yield of 3.4 and 2.9 t ha-1 in the Delta and MVZ, respectively, with however a particularly high sensitivity to unfavourable environment, its yield decreasing to 0.4 t ha-1.\nThe variety was very sensitive to unfavourable environment with yield decreasing to 1.62 t ha-1 in GLZ, as indicated by the coefficient of regression of 1.4190 (>1) and the negative constant of interception. With the highest general average of 3.3 t ha-1, GC8-35 variety showed exceptional capacities to exploit their production potential, as shown by the deviation of +21.6 and +56% in Delta and MVZ, respectively.\nSix groundnut varieties (Fleur 11, 55-437, 73-33, 78-936, GC8-35 and Hative de Sefa) were tested in three agroecological zones of Senegal River Valley (SRV) (Delta, Guiers Lake zone (GLZ) and Middle Valley zone (MVZ)) during the dry season 2002 to evaluate their yield performance and stability.\nThe variety 55-437 yielded more than 3 t ha-1 in the Delta and MVZ, whereas 73-33 reached 3 t ha-1 only in the Delta.\nBiomass production in the MVZ was particularly marked by the varieties 73-33 and 55-437, producing 15.3 and 12.8 t ha-1, respectively (Table 3).\nThe 78-936 variety, known for its weak above ground biomass production, yielded only 5.7 t ha-1. Hative de Sefa and 73-33 with respective averages of 10.3 and 11.1 t ha-1 dominated the biomass production across sites, whereas 78-936 arrived with the weakest biomass, reaching only 4.9 t ha-1.\nThe varieties 55-437 and GC8-35 with regression coefficients closest to unit (1.175 and 1.168, respectively) were regarded as relatively stable considering their biomass."
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          val parsingResults = processor.parse(text)
          val targetMentions = parsingResults.allMentions
          val contentMentions = targetMentions.filter(m => m.label == "Crop" && m.foundBy.contains("++varietyToTBM") )
          for (m <- contentMentions) {
            pw.print(s"${filename}\t${m.label}\t${m.foundBy}\t${m.sentenceObj.getSentenceText}\t${m.text}")
            if (!m.isInstanceOf[TextBoundMention]) {
              for ((key, values) <- m.arguments) {
                if (values.nonEmpty) {
                  // multiple args of same type are "::"-separated
                  val value = values.map(_.text.trim().replace("\t", "")).mkString("::")
                  pw.print(s"\t$key:\t$value")
                }
              }
            }

            pw.println()
          }
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
  }
}