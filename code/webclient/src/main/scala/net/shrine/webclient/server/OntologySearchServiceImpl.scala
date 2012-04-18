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
  // TODO: un-hard-code
  private def dao: OntologyDAO = new ShrineSqlOntologyDAO(new FileInputStream("/home/clint/workspace-3.6.2/shrine-gwt-webclient/ontology/core/ShrineWithSyns.sql"))
  
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
    
    val node = new OntNode(rootTerm, OntologyTrie.split(rootTerm).last, isLeaf(subTrie))
    
    import scala.collection.JavaConverters._
    
    /*val childNodes = subTrie.children.flatMap(getTreeRootedAt(_).asScala)
    
    node.setChildren(Helpers.toJava(childNodes))*/
    
    JCollections.singletonList(node)
  }
  
  override def getParentOntTree(term: String): JList[OntNode] = {
    import OntologyTrie._
    
    val parts = split(term)
    
    def toTerm(s: String) = addLeadingSlashesIfNeeded(addTrailingSlashIfNeeded(s))
    
    if(parts.size == 1) {
      return JCollections.singletonList(new OntNode(term, parts.last, true))
    } else {
      val parentTerm = toTerm(unSplit(parts.dropRight(1)))
      
      getTreeRootedAt(parentTerm)
    }
  }
}