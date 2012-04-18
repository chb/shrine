package net.shrine.adapter

import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.junit.Test
import net.shrine.protocol.RunQueryRequest
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.OccuranceLimited
import net.shrine.protocol.query.Term
import net.shrine.adapter.translators.QueryDefinitionTranslator
import net.shrine.protocol.query.Or
import net.shrine.adapter.translators.ExpressionTranslator

/**
 * @author Bill Simons
 * @date 4/19/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class RunQueryAdapterTest extends AssertionsForJUnit with ShouldMatchersForJUnit {
  @Test
  def testTranslateQueryDefinitionXml {
    val localTerms = Set("local1a", "local1b")
    
    val mappings = Map("network" -> localTerms)
    
    val translator = new QueryDefinitionTranslator(new ExpressionTranslator(mappings))
    
    val adapter = new RunQueryAdapter("", null, null, translator, null, true)
    
    val queryDefinition = QueryDefinition("10-17 years old@14:39:20", OccuranceLimited(1, Term("network")))
    
    val newDef = adapter.conceptTranslator.translate(queryDefinition)

    val expected = QueryDefinition("10-17 years old@14:39:20", OccuranceLimited(1, Or(Term("local1a"), Term("local1b"))))

    newDef should equal(expected) 
  }
}