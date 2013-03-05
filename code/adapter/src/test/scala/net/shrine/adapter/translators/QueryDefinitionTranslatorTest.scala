package net.shrine.adapter.translators

import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.junit.AssertionsForJUnit
import junit.framework.TestCase
import net.shrine.protocol.query.Term
import net.shrine.protocol.query.Expression
import net.shrine.protocol.query.QueryDefinition
import net.shrine.adapter.AdapterMappingException
import net.shrine.config.AdapterMappings
import net.shrine.protocol.query.Or

/**
 * @author Clint Gilbert
 * @date Mar 1, 2012
 *
 */
final class QueryDefinitionTranslatorTest extends TestCase with AssertionsForJUnit with ShouldMatchersForJUnit {
  private val localTerms = Set("localTerm1", "localTerm2")

  private val mappings = Map("twoMatches" -> localTerms, "oneMatch" -> Set("localTerm3"))

  private def queryDef(expr: Expression) = QueryDefinition("foo", expr)

  private val adapterMappings = AdapterMappings(mappings)

  def testConstructor {
    val translator = new QueryDefinitionTranslator(new ExpressionTranslator(mappings))

    assert(translator.expressionTranslator.mappings === mappings)
  }

  def testTranslate {
    val translator = new QueryDefinitionTranslator(new ExpressionTranslator(mappings))
    
    assert(translator.translate(queryDef(Term("oneMatch"))) === queryDef(Term("localTerm3")))
    assert(translator.translate(queryDef(Term("twoMatches"))) === queryDef(Or(Term("localTerm1"), Term("localTerm2"))))
  }

  def testOnFailedMapping {
    val unmapped = queryDef(Term("alskjklasdjl"))

    {
      val translatorThatThrows = new QueryDefinitionTranslator(new ExpressionTranslator(mappings))

      intercept[AdapterMappingException] {
        translatorThatThrows.translate(unmapped)
      }
    }

    {
      val anotherTranslatorThatThrows = new QueryDefinitionTranslator(new ExpressionTranslator(mappings))

      intercept[AdapterMappingException] {
        anotherTranslatorThatThrows.translate(unmapped)
      }
    }

    var mappingMissed = false

    val translator = new QueryDefinitionTranslator(new ExpressionTranslator(mappings, term => { mappingMissed = true; term }))

    assert(mappingMissed === false)

    translator.translate(queryDef(Term("oneMatch")))

    assert(mappingMissed === false)

    translator.translate(queryDef(Term("twoMatches")))

    assert(mappingMissed === false)

    val translated = translator.translate(unmapped)

    assert(translated === unmapped)
    assert(mappingMissed === true)
  }
}