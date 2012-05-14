package net.shrine.ont.data

import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import net.shrine.ont.messaging.Concept

/**
 * @author Clint Gilbert
 * 
 * @date Feb 8, 2012
 */
@Test
final class ShrineSqlOntologyDAOTest extends ShouldMatchersForJUnit with AssertionsForJUnit  {
  @Test
  def testGuards {
    intercept[IllegalArgumentException] {
      new ShrineSqlOntologyDAO(null)
    }
  }
  
  @Test
  def testLoadAllEntries {
    val shrineSqlStream = this.getClass.getClassLoader.getResourceAsStream("testShrineWithSyns.sql")
    
    val concepts = (new ShrineSqlOntologyDAO(shrineSqlStream)).ontologyEntries
    
    concepts.size should equal(659) //magic, number of entries in src/test/resources/ShrineWithSyns.sql
    
    val fordyceConcepts = concepts.filter(c => c.path.toLowerCase.contains("fordyce") || c.synonym.exists(_.toLowerCase.contains("fordyce"))).toList
    
    val expected = Seq(Concept("""\\SHRINE\SHRINE\Diagnoses\Diseases of the skin and subcutaneous tissue\Other skin disorders\Fox-Fordyce disease\""", Some("Fox-Fordyce disease")),
    				   Concept("""\\SHRINE\SHRINE\Diagnoses\Diseases of the skin and subcutaneous tissue\Other skin disorders\Fox-Fordyce disease\""", Some("Fox-Fordyce disease")),
    				   Concept("""\\SHRINE\SHRINE\Diagnoses\Diseases of the skin and subcutaneous tissue\Other skin disorders\Fox-Fordyce disease\""", Some("Fordyce-Fox disease")),
    				   Concept("""\\SHRINE\SHRINE\Diagnoses\Diseases of the skin and subcutaneous tissue\Other skin disorders\Fox-Fordyce disease\""", Some("Apocrine miliaria")))
    				   
    val Seq(firstExpected, secondExpected, thirdExpected, fourthExpected) = expected
    
    val Seq(firstActual, secondActual, thirdActual, fourthActual) = expected
    				 
    firstActual should equal(firstExpected)
    secondActual should equal(secondExpected)
    thirdActual should equal(thirdExpected)
    fourthActual should equal(fourthExpected)
    
    fordyceConcepts should equal(expected)
  }
}