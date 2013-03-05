package net.shrine.adapter.translators
import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.junit.AssertionsForJUnit
import junit.framework.TestCase
import net.shrine.config.AdapterMappings
import net.shrine.protocol.query.Term
import net.shrine.protocol.query.Or
import net.shrine.adapter.AdapterMappingException
import net.shrine.protocol.query.Expression
import net.shrine.protocol.query.Not
import net.shrine.protocol.query.And
import org.spin.tools.NetworkTime
import java.util.GregorianCalendar
import net.shrine.protocol.query.DateBounded
import net.shrine.protocol.query.OccuranceLimited

/**
 * @author Clint Gilbert
 * @date Mar 1, 2012
 *
 */
final class ExpressionTranslatorTest extends TestCase with AssertionsForJUnit with ShouldMatchersForJUnit {
  private val localTerms = Set("localTerm1", "localTerm2")

  private val mappings = Map("twoMatches" -> localTerms, "oneMatch" -> Set("localTerm3"))

  private val adapterMappings = AdapterMappings(mappings)

  def testConstructorScalaMap {
    val translator = new ExpressionTranslator(mappings)

    assert(translator.mappings === mappings)
  }

  def testConstructorAdapterMappings {
    val translator = ExpressionTranslator(adapterMappings)

    assert(translator.mappings === mappings)
  }
  
  private def doTestTranslate(Op: Expression => Expression) {
    val translator = new ExpressionTranslator(mappings)
    
    val translated = translator.translate(Op(Term("twoMatches")))
    
    assert(translated === Op(Or(localTerms.toSeq.map(Term(_)): _*)))
    
    intercept[AdapterMappingException] {
      translator.translate(Op(Term("alskjklasdjl")))
    }
    
    val oneMatch = translator.translate(Op(Term("oneMatch")))
    
    assert(oneMatch === Op(Term("localTerm3")))
  }
  
  def testTranslateTerm = doTestTranslate(x => x)
  
  def testTranslateNot = doTestTranslate(Not)
  
  def testTranslateAnd = doTestTranslate(And(_))
  
  def testTranslateOr = doTestTranslate(Or(_))

  private def doTestTranslateMultiExprs(Op: (Expression*) => Expression) = {
    val translator = new ExpressionTranslator(mappings)
    
    val expr = Op(Term("twoMatches"), Term("twoMatches"))
    
    val translated = translator.translate(expr)
    
    val expectedOr = Or(localTerms.toSeq.map(Term(_)): _*)
    
    assert(translated === Op(expectedOr, expectedOr))
  }
  
  def testTranslateAndMultiExprs = doTestTranslateMultiExprs(exprs => And(exprs: _*))
  
  def testTranslateOrMultiExprs = doTestTranslateMultiExprs(exprs => Or(exprs: _*))
  
  private val now = Some(NetworkTime.makeXMLGregorianCalendar(new GregorianCalendar))
  
  def testTranslateDateBounded = doTestTranslate(DateBounded(now, now, _))
  
  def testTranslateOccurranceLimited = doTestTranslate(OccuranceLimited(99, _))
  
  def testTranslateComplexExpr = doTestTranslate(expr => Or(And(DateBounded(now, now, OccuranceLimited(99, expr)))))
  
  def testOnFailedMapping {
    var mappingMissed = false
    
    val translator = new ExpressionTranslator(mappings, term => { mappingMissed = true ; term })
    
    assert(mappingMissed === false)
    
    translator.translate(Term("oneMatch"))
    
    assert(mappingMissed === false)
    
    translator.translate(Term("twoMatches"))
    
    assert(mappingMissed === false)
    
    val unmapped = Term("alskjklasdjl")
    
    val translated = translator.translate(unmapped)
    
    assert(translated === unmapped)
    assert(mappingMissed === true)
  }
}