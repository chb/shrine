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

/**
 * @author clint
 * @date Mar 23, 2012
 */
final class OntologySearchServiceImpl extends RemoteServiceServlet with OntologySearchService {
  private def dao: OntologyDAO = new ShrineSqlOntologyDAO(getOntFileStream)
  
  private val index: OntologyIndex = LuceneOntologyIndex(dao)

  private val ontTrie = OntologyTrie(dao)

  private def isLeaf(trie: OntologyTrie): Boolean = trie.children.isEmpty
  
  private def isLeaf(c: Concept): Boolean = isLeaf(ontTrie.subTrieForPrefix(c.path))
  
  override def getSuggestions(typedSoFar: String, limit: Int): JList[TermSuggestion] = {
    val concepts = index.search(typedSoFar).take(limit)

    val suggestions = concepts.map(c => new TermSuggestion(c.path, c.simpleName, typedSoFar, c.synonym.orNull, c.category, isLeaf(c)))

    Helpers.toJava(suggestions)
  }
  
  override def getChildrenFor(term: String): JList[OntNode] = {
    if(!ontTrie.contains(term)) {
      println("Couldn't find term '" + term + "'")
      
      return JCollections.emptyList[OntNode]
    }
    
    val subTrie = ontTrie.subTrieForPrefix(term)
    
    import scala.collection.JavaConverters._
    
    val childNodes = subTrie.children.flatMap(getTreeRootedAt(_).asScala)
    
    val childNodesSorted = childNodes.toSeq.sortBy(_.getSimpleName)
    
    Helpers.toJava(childNodesSorted)
  }
  
  override def getTreeRootedAt(term: String): JList[OntNode] = {
    if(!ontTrie.contains(term)) {
      println("Couldn't find term '" + term + "'")
      
      return JCollections.emptyList[OntNode]
    }
    
    val subTrie = ontTrie.subTrieForPrefix(term)
    
    val rootTerm = subTrie.head
  
    val node = new OntNode(toTerm(rootTerm), isLeaf(subTrie))
    
    JCollections.singletonList(node)
  }
  
  private def toTerm(path: String): Term = {
    val (category, simpleName) = {
      val parts = OntologyTrie.split(path)
      
      val withoutSHRINE = parts.dropWhile(_ == "SHRINE")
      
      val category = if(withoutSHRINE.isEmpty) "" else withoutSHRINE.head
        
      (category, parts.last)
    }
    
    new Term(path, category, simpleName)
  }
  
  override def getParentOntTree(term: String): JList[OntNode] = {
    import OntologyTrie._
    
    val parts = split(term)
    
    def addSlashes(s: String) = addLeadingSlashesIfNeeded(addTrailingSlashIfNeeded(s))
    
    if(parts.size == 1) {
      return JCollections.singletonList(new OntNode(toTerm(addSlashes(term)), true))
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