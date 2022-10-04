package org.clulab.habitus

import org.clulab.habitus.entitynetworks.LexiconNerBase
import org.clulab.habitus.utils.Test
import org.clulab.habitus.variables.VariableProcessor
import org.clulab.processors.Sentence
import org.clulab.processors.clu.CluProcessor

class TestHabitusProcessor extends Test {

  behavior of "HabitusProcessor"

  it should "process Unicode correctly" in {
    val variableProcessor = VariableProcessor().reloaded // now uses a HabitusProcessor
    val text = HabitusTokenizer.endash + "1"

    val parsingResults = variableProcessor.parse(text)
    val doc = parsingResults.document
    val words = doc.sentences.head.words
    val raw = doc.sentences.head.raw

    words.head.substring(0, 1) should be (HabitusTokenizer.dash)
    // This does not pass.  We are changing the raw text!
    // raw.head.substring(0, 1) should be (HabitusTokenizer.endash)
  }

  behavior of "HabitusProcessor.isBadSentence"

  {
    // These examples are taken from FCR_SRVRice.txt that was converted with ScienceParse.
    // Headers on the pages seem to be particularly problematic.
    val cluProcessor = new CluProcessor()
    val habitusProcessor = new HabitusProcessor(None)
    val controlText = "Numerous water-saving technologies for rice have been validated in Asia, though they remain relatively untested and are not yet recommended anywhere in Africa."

    def test(text: String, isBad: Boolean): Unit = {
      val name = s"""${if (isBad) "" else "not "}remove potentially malformed sentence "$text""""

      it should name in {
        val cluDocument = cluProcessor.mkDocument(text)

        cluDocument.sentences.size should be (1)
        habitusProcessor.isBadSentence(cluDocument.sentences.head) should be (isBad)
      }
    }

    val texts = Array(
      // Line numbers are from the var-reader-extractions spreadsheet.
      /* mostSingleLettersInARow */
      /* 63 */ "However, Senegalese 1 ps Res f t t ( d ( g 2 d y r A c m y i p n ( f p u e m c 2 a p p b M a ( p S a t t i s 2 p R t S f g q f e t b t s q i i m e e m h .",
      /* 51 */ "Krupnik et al. / Field Crops Research 130 (2012) 155-167 157 1 ps Res m a d t o s 2 t c Q w H V v h r o t t d u i s t t i r N a a t i c o H t A T i a d e 2 v ( c f t h c .",
      /* 36 */ "Krupnik et al. / Field Crops Research 130 (2012) 155-167 159 - + 1 n n t s p s R I i + ( t p i s s f s c o w i s R d s f S f w e a 4 bove columns (SYSTEM, Crop Management System; +STRAW, with straw; -STRAW onnected by underlines or separated by letters are not significantly different accor ST-F and +ST-F yields (2.9 and 3.8 t ha-1) alongside a 8.0 t ha-1 ST+F yield, while yield under the same RMP sub-treatment was 0% less than SRI.",
      /* 29 */ "Krupnik et al. / Field Crops Research 130 (2012) 155-167 b - m b r t c s f p s g u s i t a s e w R l s i t T T t S RMP) and the System of Rice Intensification (SRI) under four fertility treatments ertilizer) in Ndiaye, Senegal.",
      /* 57 */ "ans within a continuous column are not significantly different using the LS means ps Res t 2 a u h b t s w i f s e y r t c i t + t r w s s a p i + a A e 2 i s r a h C t s r + c s r H r s e b t w i K t K T.",
      /* 39 */ "Our results indicate that substantial water savings and increases in water productivity can be obtained with SRI, although significant yield increases compared to RMP should 1 ps Res n f s N t fia t a i t y r c w m p n t t A F t P t S C B ( i A R A B B B B B B B C C C C C .",
      /* Line 359 */ "Pl an td en si ty ( n b m -2 ) C la y + S ilt ( % ) C ( / 1000 ) O M ( / 1000 ) O rg an ic N ( / 1000 ) pH w at er 060 da s 60 -9 0 da s 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 20 12 D ar ou C / V x x x x x x x x x x x x x x x x x x 20 / 0 7 F 1 / F2 44 9 .",

      // So far, this has been almost every word in the sentence.  The limit may not work well for short sentences which can't make the limit.
      /* mostCustomKernsInARow */
      /* 135 */ "CSM-CERES-Rice Ta bl e 1 Y ea r , si te , c ro p m an ag em en t , so il pr op er tie s , an d ra in fa ll di st ri bu tio n in th e ex pe ri m en ts in S en eg al us ed fo r D S SA T ca lib ra tio n an d va lid at io n. da s da ys af te r so w in g , F er tf er til iz er tr ea tm en t , w ith F1 : r ec om m en de d do se ( 8 0 kg N ha -1 ) , i.e ., 20 0 kg ha -1 N PK ( 1 5 .",
      /*  52 */ "Y ea r S ite U se C ul tu ra lp ra ct ic es So il pr op er tie s ( 0 -3 0 cm ) R ai nf al l ( m m ) C ul tiv ar s So w in g da te Fe rt .",

      // too short, under 3 words
      "I am",
      // too long, over 150 words
      "I am " * 75 + ".",
      // too non-alpha-ie with less than half letters
      // 3 with alpha and 8 total, 3 < 8 / 2, so bad
      "11 22 33 44 do re me.",
      /* Line 296 */
      "mg kg-1 ) Exchangeable K ( cmol kg-1 ) EC ( dS m-1 ) pH 0.54 1.03 5.42 0.50 1.02 5.68 0.49 b 0.91 5.45 0.55 a 1.11 5.60 2.76 ns 0.00 ns 1.52 ns 4.83 * 2.78 ns 0.64 ns tion , crop management system x straw application , or crop management sysd here .",
    )

    test(controlText, false) // Don't throw them all out!
    texts.foreach { text => test(text, true) }
  }

  behavior of "acronym"

  it should "not be placed in the middle of an existing BII sequence" in {
    val text = "Saed.sn Saed BP and a few more words. " // maz: to avoid headlines, disallowing sentences that have > 0.6 capitalized words
    val lexiconNer = new LexiconNerBase().mkLexiconNer()
    val processor = new HabitusProcessor(Some(lexiconNer))
    val document = processor.annotate(text)
    val entities = document.sentences.head.entities.get.mkString(" ")
    entities should be ("B-ORG I-ORG I-ORG O O O O O O")
  }
}
