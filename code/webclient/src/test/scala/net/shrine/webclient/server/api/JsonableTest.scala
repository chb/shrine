package net.shrine.webclient.server.api

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import net.shrine.webclient.shared.domain.Term
import net.shrine.webclient.shared.domain.OntNode
import net.shrine.webclient.shared.domain.TermSuggestion
import net.shrine.webclient.server.MultiInstitutionQueryResult
import net.shrine.webclient.shared.domain.BootstrapInfo

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

  @Test
  def testMultiInstitutionQueryResultIsJsonable {
    val result = MultiInstitutionQueryResult(Map("foo" -> 123, "bar" -> 987654))
    
    doRoundTrip(result)()
    
    val expectedJson = """{"foo":123,"bar":987654}"""
      
    toJson(result) should equal(expectedJson)
    
    val empty = MultiInstitutionQueryResult(Map.empty)
    
    doRoundTrip(empty)()
    
    toJson(empty) should equal("{}")
  }
  
  private def toJson[T : Jsonable](thing: T): String = implicitly[Jsonable[T]].toJsonString(thing)
  
  private def checkTermEquality(expected: Term, actual: Term) {
    expected.path should equal(actual.path)
    expected.category should equal(actual.category)
    expected.simpleName should equal(actual.simpleName)
  }

  private def escapeSlashes(s: String) = s.replaceAll("""\\""", """\\\\""")

  private def doRoundTrip[T: Jsonable](thing: T)(equalityChecker: (T, T) => Unit = (expected: T, actual: T) => actual should equal(expected)) {
    val serializer = implicitly[Jsonable[T]]

    val json = serializer.toJsonString(thing)

    val unmarshalled = serializer.fromJsonString(json).get

    equalityChecker(thing, unmarshalled)
  }
}