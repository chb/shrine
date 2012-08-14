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
    val expected = Seq(Concept("""\\SHRINE\SHRINE\Demographics\Race\American Indian or Alaska Native\American Indian\Sac and Fox\""", Some("Sac and Fox")),
    				   Concept("""\\SHRINE\SHRINE\Demographics\Race\American Indian or Alaska Native\American Indian\Sac and Fox\Iowa Sac and Fox\""", Some("Iowa Sac and Fox")),
    				   Concept("""\\SHRINE\SHRINE\Demographics\Race\American Indian or Alaska Native\American Indian\Sac and Fox\Missouri Sac and Fox\""", Some("Missouri Sac and Fox")),
    				   Concept("""\\SHRINE\SHRINE\Demographics\Race\American Indian or Alaska Native\American Indian\Sac and Fox\Oklahoma Sac and Fox\""", Some("Oklahoma Sac and Fox")),
    				   Concept("""\\SHRINE\SHRINE\Diagnoses\Diseases of the skin and subcutaneous tissue\Other skin disorders\Fox-Fordyce disease\""", Some("Fox-Fordyce disease"), Some("705.82")))

    //Search term that returns a smallish number of things, some of which are found through synonymy 

    val Seq(expected1, expected2, expected3, expected4, expected5) = expected     				   
    				   
    val actual = index.search("fox")
    				   
    val Seq(actual1, actual2, actual3, actual4, actual5) = actual
    
    actual1 should equal(expected1)
    actual2 should equal(expected2)
    actual3 should equal(expected3)
    actual4 should equal(expected4)
    actual5 should equal(expected5)
    
    actual should equal(expected)

    //Search on a synonym

    val expectedForSynonym = Seq(Concept("""\\SHRINE\SHRINE\Diagnoses\Diseases of the skin and subcutaneous tissue\Other skin disorders\Fox-Fordyce disease\""", Some("Apocrine miliaria"), Some("705.82")))

    index.search("apocrine") should equal(expectedForSynonym)
  }
  
  @Test
  def testSearchingByICD9Code {
    val expected = Seq(Concept("""\\SHRINE\SHRINE\Diagnoses\Diseases of the skin and subcutaneous tissue\Other skin disorders\Fox-Fordyce disease\""", Some("Fox-Fordyce disease"), Some("705.82")))
    
    index.search("705.82") should equal(expected)
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
