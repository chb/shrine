package net.shrine.webclient.server.api

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import net.shrine.webclient.shared.domain.Term
import net.shrine.webclient.shared.domain.OntNode
import net.shrine.webclient.shared.domain.TermSuggestion
import net.shrine.webclient.shared.domain.BootstrapInfo
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.protocol.ResultOutputType
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult
import net.shrine.webclient.shared.domain.MultiInstitutionQueryResult
import net.shrine.webclient.server.Helpers
import net.shrine.webclient.shared.domain.Breakdown

/**
 * @author clint
 * @date Aug 7, 2012
 */
final class JsonableTest extends TestCase with AssertionsForJUnit with ShouldMatchers {
  @Test
  def testBootstrapInfoIsJsonable {
    val username = "aksldksalfjklasfjklasfjkl"
      
    val bootstrapInfo = new BootstrapInfo(username)
    
    doRoundTrip(bootstrapInfo)()
    
    toJson(bootstrapInfo) should equal("""{"loggedInUsername":"""" + username + """"}""")
  }
  
  @Test
  def testTermIsJsonable {
    val path = """\\SHRINE\SHRINE\Foo\Bar\"""
    val category = "some-category"
    val simpleName = "some-simple-name"

    val term = new Term(path, category, simpleName)

    doRoundTrip(term)(checkTermEquality)

    //TODO: Why is it necessary to escape the path?
    toJson(term) should equal("""{"path":"""" + escapeSlashes(path) + """","category":"""" + category + """","simpleName":"""" + simpleName + "\"}")
  }

  @Test
  def testOntNodeIsJsonable {
    val path = """\\SHRINE\SHRINE\Foo\Bar\"""
    val category = "some-category"
    val simpleName = "some-simple-name"

    val term = new Term(path, category, simpleName)
    
    import scala.collection.JavaConverters._
    
    val child0 = new OntNode(term, Seq.empty[OntNode].asJava, true)
    val child1 = new OntNode(term, Seq.empty[OntNode].asJava, true)
    
    val node = new OntNode(term, Seq(child0, child1).asJava, false)
    
    doRoundTrip(node)()
    
    val expectedJson = """{"term":{"path":"""" + escapeSlashes(path) + """","category":"""" + category + """","simpleName":"""" + simpleName + """"},"isLeaf":false,"children":[{"term":{"path":"""" + escapeSlashes(path) + """","category":"""" + category + """","simpleName":"""" + simpleName + """"},"isLeaf":true,"children":[]},{"term":{"path":"""" + escapeSlashes(path) + """","category":"""" + category + """","simpleName":"""" + simpleName + """"},"isLeaf":true,"children":[]}]}"""
    
    toJson(node) should equal(expectedJson)
  }
  
  @Test
  def testTermSuggestionIsJsonable {
    val path = """\\SHRINE\SHRINE\Foo\Bar\"""
    val simpleName = "some-simple-name"
    val highlight = "foo"
    val synonym = "smoe-synonym"
    val category = "some-category"
    val isLeaf = true
    
    val suggestion = new TermSuggestion(path, simpleName, highlight, synonym, category, isLeaf)
    
    doRoundTrip(suggestion)()
    
    val expectedJson = """{"path":"""" + escapeSlashes(path) + """","simpleName":"""" + simpleName + """","highlight":"""" + highlight + """","synonym":"""" + synonym + """","category":"""" + category + """","isLeaf":""" + isLeaf + """}"""
      
    toJson(suggestion) should equal(expectedJson)
  }

  import Helpers._
  
  import scala.collection.JavaConverters._
  
  @Test
  def testSingleInstitutionResultIsJsonable {
    val breakdownData = new Breakdown((0 to 4).map(i => ("column_" + i, java.lang.Long.valueOf(i))).toMap.asJava)
    
    import ResultOutputType._
    
    val result = new SingleInstitutionQueryResult(999, Map(PATIENT_GENDER_COUNT_XML.name -> breakdownData,
                                                            PATIENT_RACE_COUNT_XML.name -> breakdownData).asJava, false)
    
    doRoundTrip(result)()
    
    toJson(new SingleInstitutionQueryResult(1234, Map.empty.asJava, true)) should equal("""{"count":1234,"breakdowns":{},"isError":true}""")
  }
  
  @Test
  def testMultiInstitutionQueryResultIsJsonable {
    def singleInstResult(count: Long): SingleInstitutionQueryResult = {
      new SingleInstitutionQueryResult(count, makeBreakdownsByTypeMap(ResultOutputType.values.map { resultType =>
        I2b2ResultEnvelope(resultType, (0 to 4).map { i =>
          I2b2ResultEnvelope.Column("column_" + i, i)
        })
      }), false)
    }
    
    val result = new MultiInstitutionQueryResult(Map("foo" -> singleInstResult(123), "bar" -> singleInstResult(987654)).asJava)
    
    doRoundTrip(result)()
    
    val empty = new MultiInstitutionQueryResult(Map.empty.asJava)
    
    doRoundTrip(empty)()
    
    toJson(empty) should equal("""{"results":{}}""")
  }
  
  private def toJson[T : Jsonable](thing: T): String = implicitly[Jsonable[T]].toJsonString(thing)
  
  private def checkTermEquality(expected: Term, actual: Term) {
    expected.getPath should equal(actual.getPath)
    expected.getCategory should equal(actual.getCategory)
    expected.getSimpleName should equal(actual.getSimpleName)
  }

  private def escapeSlashes(s: String) = s.replaceAll("""\\""", """\\\\""")

  private def doRoundTrip[T: Jsonable](thing: T)(equalityChecker: (T, T) => Unit = (expected: T, actual: T) => actual should equal(expected)) {
    val serializer = implicitly[Jsonable[T]]
    
    val json = serializer.toJsonString(thing)

    val unmarshalled = serializer.fromJsonString(json).get

    equalityChecker(thing, unmarshalled)
  }
}