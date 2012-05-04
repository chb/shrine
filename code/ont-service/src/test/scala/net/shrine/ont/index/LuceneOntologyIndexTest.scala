package net.shrine.ont.index

import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import net.shrine.ont.data.ShrineSqlOntologyDAO
import net.shrine.ont.messaging.Concept
import junit.framework.TestCase

/**
 * @author Clint Gilbert
 *
 * @date Feb 8, 2012
 */
@Test
final class LuceneOntologyIndexTest extends TestCase with ShouldMatchersForJUnit with AssertionsForJUnit {
  private var index: OntologyIndex = _

  @Test
  def testBasicSearch {
    val expected = Seq(Concept("""\\SHRINE\SHRINE\Diagnoses\Diseases of the skin and subcutaneous tissue\Other skin disorders\Fox-Fordyce disease\""", Some("Fox-Fordyce disease")),
    				   Concept("""\\SHRINE\SHRINE\Demographics\Race\American Indian or Alaska Native\American Indian\Sac and Fox\""", None),
    				   Concept("""\\SHRINE\SHRINE\Demographics\Race\American Indian or Alaska Native\American Indian\Sac and Fox\Iowa Sac and Fox\""", None),
    				   Concept("""\\SHRINE\SHRINE\Demographics\Race\American Indian or Alaska Native\American Indian\Sac and Fox\Missouri Sac and Fox\""", None),
    				   Concept("""\\SHRINE\SHRINE\Demographics\Race\American Indian or Alaska Native\American Indian\Sac and Fox\Oklahoma Sac and Fox\""", None))

    //Search term that returns a smallish number of things, some of which are found through synonymy 

    index.search("fox") should equal(expected)

    //Search on a synonym

    val expectedForSynonym = Seq(Concept("""\\SHRINE\SHRINE\Diagnoses\Diseases of the skin and subcutaneous tissue\Other skin disorders\Fox-Fordyce disease\""", Some("Apocrine miliaria")))

    index.search("apocrine") should equal(expectedForSynonym)
  }

  override def setUp() {
    this.index = {
      val shrineSqlStream = this.getClass.getClassLoader.getResourceAsStream("testShrineWithSyns.sql")

      try {
        LuceneOntologyIndex(new ShrineSqlOntologyDAO(shrineSqlStream))
      } finally {
        shrineSqlStream.close()
      }
    }
  }

  override def tearDown() {
    index.shutDown()
  }
}
