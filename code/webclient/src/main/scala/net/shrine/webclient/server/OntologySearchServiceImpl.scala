package net.shrine.webclient.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet
import net.shrine.webclient.client.OntologySearchService
import java.util.{ Collections => JCollections }
import java.util.{ List => JList }
import net.shrine.ont.index.OntologyIndex
import net.shrine.ont.index.LuceneOntologyIndex
import net.shrine.ont.data.ShrineSqlOntologyDAO
import java.io.FileInputStream
import net.shrine.webclient.client.domain.Term
import net.shrine.webclient.client.domain.TermSuggestion
import net.shrine.ont.data.OntologyDAO
import net.shrine.ont.index.OntologyTrie
import net.shrine.webclient.client.domain.OntNode
import net.shrine.ont.messaging.Concept
import scala.util.matching.Regex

/**
 * @author clint
 * @date Mar 23, 2012
 */
final class OntologySearchServiceImpl extends RemoteServiceServlet with OntologySearchService {
  private def dao: OntologyDAO = new ShrineSqlOntologyDAO(getOntFileStream)
  
  private val index: OntologyIndex = LuceneOntologyIndex(dao)

  private val ontTrie = OntologyTrie(dao)

  private def isLeaf(trie: OntologyTrie): Boolean = trie.children.isEmpty
  
  private def isLeaf(concept: Concept): Boolean = isLeaf(ontTrie.subTrieForPrefix(concept))
  
  private [this] val allDigitRegex = """^\d+$""".r
  
  //Matches things like 'AD100'
  private [this] val medicationCategoryRegex = """^\w\w\d\d\d$""".r
  
  private def determineSimpleNameFor(concept: Concept): String = {
    val simpleName = concept.simpleName
    
    def simpleNameMatches(regex: Regex) = regex.findFirstIn(simpleName).isDefined
    
    def isAllDigits = simpleNameMatches(allDigitRegex)

    def isMedicationCategory = simpleNameMatches(medicationCategoryRegex)
    
    def isLab = concept.category == "Labs"

    val isCodedValue = isAllDigits || isMedicationCategory || isLab
    
    //Return the synonym as the simple name if the simple name is a coded value and a synonym is present; 
    //otherwise, return the simple name unaltered.
    //Useful for medications, which have all-digit simple names, but human-readable synonyms
    if(isCodedValue) concept.synonym.getOrElse(simpleName) else simpleName
  }
  
  override def getSuggestions(typedSoFar: String, limit: Int): JList[TermSuggestion] = {
    val concepts = index.search(typedSoFar).take(limit)

    def toTermSuggestion(c: Concept): TermSuggestion = new TermSuggestion(c.path, determineSimpleNameFor(c), typedSoFar, c.synonym.orNull, c.category, isLeaf(c))
    
    val suggestions = concepts.map(toTermSuggestion)

    Helpers.toJava(suggestions)
  }
  
  private def toConcept(path: String) = Concept(path, None)
  
  override def getChildrenFor(term: String): JList[OntNode] = {
    val concept = toConcept(term)
    
    if(!ontTrie.contains(concept)) {
      println("Couldn't find term '" + term + "'")
      
      return JCollections.emptyList[OntNode]
    }
    
    val subTrie = ontTrie.subTrieForPrefix(concept)
    
    import scala.collection.JavaConverters._
    
    val childNodes = subTrie.children.flatMap { c =>
      //println("Child path: " + c.path)
      
      val children = getTreeRootedAt(c.path).asScala
      
      //println("Child: " + children.head)
      
      children
    }
    
    val childNodesSorted = childNodes.toSeq.sortBy(_.getSimpleName)
    
    Helpers.toJava(childNodesSorted)
  }
  
  override def getTreeRootedAt(term: String): JList[OntNode] = {
    val concept = toConcept(term)
    
    if(!ontTrie.contains(concept)) {
      println("Couldn't find term '" + term + "'")
      
      return JCollections.emptyList[OntNode]
    }
    
    val subTrie = ontTrie.subTrieForPrefix(concept)
    
    val rootConcept = subTrie.head
  
    val node = new OntNode(toTerm(rootConcept), isLeaf(subTrie))
    
    JCollections.singletonList(node)
  }
  
  private def toTerm(concept: Concept) = new Term(concept.path, concept.category, determineSimpleNameFor(concept))
  
  override def getParentOntTree(path: String): JList[OntNode] = {
    import OntologyTrie._
    
    def addSlashes(s: String) = addLeadingSlashesIfNeeded(addTrailingSlashIfNeeded(s))
    
    val parts = split(path)
    
    val concept = toConcept(addSlashes(path))
    
    if(parts.size == 1) {
      return JCollections.singletonList(new OntNode(toTerm(concept), true))
    } else {
      val parentTerm = addSlashes(unSplit(parts.dropRight(1)))
      
      getTreeRootedAt(parentTerm)
    }
  }
  
  private[this] def getOntFileStream: java.io.InputStream = {
    val ontFileName = "ShrineWithSyns.sql"
    
    val stream = getClass.getClassLoader.getResourceAsStream(ontFileName)
    
    require(stream != null, "Couldn't find ontology file '" + ontFileName + "' on the classpath")
    
    stream
  }
}