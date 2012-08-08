package net.shrine.webclient.server.api

import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import com.sun.jersey.test.framework.JerseyTest
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.shrine.webclient.client.domain.TermSuggestion
import org.junit.Test
import net.shrine.webclient.server.OntologyServiceImpl
import net.shrine.webclient.server.OntologyService
import net.shrine.webclient.server.QueryService
import net.shrine.webclient.client.domain.OntNode

/**
 * @author clint
 * @date Aug 7, 2012
 */
final class OntologyServiceJaxrsTest extends JerseyTest with ShrineWebclientApiJaxrsTest {
  
  override def ontologyService: OntologyService = new OntologyServiceImpl(getClass.getClassLoader.getResourceAsStream("testShrineWithSyns.sql"))
  
  override def queryService: QueryService = null
  
  @Test
  def testSuggestions {
    
    def getSuggestions(typedSoFar: String, limit: Int) = unmarshalSeq[TermSuggestion](ontResource.path("suggestions").queryParam("typedSoFar", typedSoFar).queryParam("limit", limit.toString).get(classOf[String]))
    
    {
      val suggestions = getSuggestions("foo", 10)
    
      suggestions.size should equal(10)
    
      suggestions.forall(_.getHighlight == "foo") should be(true)
    }
    
    {
      val maleSuggestion = new TermSuggestion(male.path, "Male", "male", "Male", "Demographics", true)
    
      val femaleSuggestion = new TermSuggestion(female.path, "Female", "male", "Female", "Demographics", true)
    
      val expected = Seq(femaleSuggestion, maleSuggestion)
    
      val suggestions = getSuggestions("male", 10)

      def deepEquals(a: TermSuggestion, b: TermSuggestion) {
        a.getCategory should equal(b.getCategory)
        a.getPath should equal(b.getPath)
        a.getHighlight should equal(b.getHighlight)
        a.getSimpleName should equal(b.getSimpleName)
        a.getSynonym should equal(b.getSynonym)
      }
    
      deepEquals(suggestions.head, expected.head) 
      deepEquals(suggestions.tail.head, expected.tail.head)
    
      suggestions.size should equal(expected.size)
      suggestions should equal(expected)
    }
  }
  
  @Test
  def testChildrenOf {
    def getChildrenOf(term: String) = unmarshalSeq[OntNode](ontResource.path("children-of").queryParam("term", term).get(classOf[String]))
    
    import scala.collection.JavaConverters._
    
    val children = getChildrenOf(gender.path)
    
    val expected = Seq(female, male, undifferentiated, unknown).map(c => new OntNode(OntologyServiceImpl.toTerm(c), true))
    
    val Seq(c1, c2, c3, c4) = children
    
    val Seq(e1, e2, e3, e4) = expected
    
    c1 should equal(e1)
    c2 should equal(e2)
    c3 should equal(e3)
    c4 should equal(e4)
    
    children should equal(expected)
  }
  
  @Test
  def testPathTo {
    def getPathTo(term: String) = unmarshalSeq[OntNode](ontResource.path("path-to").queryParam("term", term).get(classOf[String]))
    
    val pathToMale = getPathTo(male.path)
    
    import OntologyServiceImpl.{toTerm, toConcept}
    
    def ontNode(path: String, isLeaf: Boolean = false) = new OntNode(toTerm(toConcept(path)), isLeaf)
    
    val expected = Seq(ontNode("""\\SHRINE\SHRINE\"""), ontNode("""\\SHRINE\SHRINE\Demographics\"""), ontNode("""\\SHRINE\SHRINE\Demographics\Gender\"""), ontNode("""\\SHRINE\SHRINE\Demographics\Gender\Male\""", true))
    
    pathToMale should equal(expected)
    
    getPathTo("foo").isEmpty should be(true)
  }
}