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
import org.apache.log4j.Logger
import java.io.InputStream

/**
 * @author clint
 * @date Mar 23, 2012
 *
 */
final class OntologySearchServiceImpl(ontologyStream: => InputStream) extends RemoteServiceServlet with OntologySearchService {
  require(ontologyStream != null)

  //Needed so this can be instantiated by an app server :/ 
  def this() = this(OntologySearchServiceImpl.ontFileStream)

  private[this] def dao: OntologyDAO = new ShrineSqlOntologyDAO(ontologyStream)

  private[this] val index: OntologyIndex = LuceneOntologyIndex(dao)

  private[this] val ontTrie = OntologyTrie(dao)

  import OntologySearchServiceImpl._

  private[server] def isLeaf(concept: Concept): Boolean = isLeafTrie(ontTrie.subTrieForPrefix(concept))

  override def getSuggestions(typedSoFar: String, limit: Int): JList[TermSuggestion] = {
    val concepts = index.search(typedSoFar).take(limit)

    def toTermSuggestion(c: Concept): TermSuggestion = new TermSuggestion(c.path, determineSimpleNameFor(c), typedSoFar, c.synonym.orNull, c.category, isLeaf(c))

    val suggestions = concepts.map(toTermSuggestion)

    Helpers.toJavaList(suggestions)
  }

  override def getChildrenFor(term: String): JList[OntNode] = {
    val concept = toConcept(term)

    if (ontTrie.contains(concept)) {
      val subTrie = ontTrie.subTrieForPrefix(concept)

      val childNodes = for {
        childConcept <- subTrie.children
        node <- toOntNode(childConcept.path)
      } yield node

      val childNodesSorted = childNodes.toSeq.sortBy(_.getSimpleName)

      Helpers.toJavaList(childNodesSorted)
    } else {
      logger.info("Couldn't get children for nonexistent term '" + term + "'")

      emptyOntNodeList
    }
  }

  private[server] def toOntNode(term: String): Option[OntNode] = {
    import OntologyTrie._

    val concept = toConcept(term)

    val subTrie = ontTrie.subTrieForPrefix(concept)

    val rootConceptOption = subTrie.headOption

    def isDesiredTerm(c: Concept) = c.path == term

    def intoOntNode(c: Concept) = new OntNode(toTerm(c), isLeafTrie(subTrie))

    val result = rootConceptOption.filter(isDesiredTerm).map(intoOntNode)

    if (result.isEmpty) {
      logger.info("Couldn't find term '" + term + "'")
    }

    result
  }

  override def getPathTo(term: String): JList[OntNode] = {
    def pathsFromRoot(termPath: String): Seq[String] = {
      import OntologyTrie._

      val parts = split(termPath)

      //[a,b,c] => [[a],[a,b],[a,b,c]], etc
      val paths = (1 to parts.size).map(i => parts.take(i))

      //Drop leading two SHRINE parts, since they're conceptually one thing
      val withoutFirstTwoSHRINEs = paths.dropWhile(_.last == "SHRINE")

      def toOntPath(parts: Seq[String]) = addSlashesIfNeeded(unSplit(parts))

      //unsplit, and re-add \\SHRINE\SHRINE\ 
      """\\SHRINE\SHRINE\""" +: withoutFirstTwoSHRINEs.map(toOntPath)
    }

    if(ontTrie.contains(toConcept(term))) {
	    val nodes = for {
	      ontTerm <- pathsFromRoot(term)
	      node <- toOntNode(ontTerm)
	    } yield node
	
	    Helpers.toJavaList(nodes)
    } else {
      logger.info("Couldn't get path to nonexistent term '" + term + "'")
      
      emptyOntNodeList
    }
  }
}

/**
 * @author clint
 * @date Mar 23, 2012
 */
object OntologySearchServiceImpl {
  private val emptyOntNodeList = JCollections.emptyList[OntNode]
  
  private val logger = Logger.getLogger(classOf[OntologySearchServiceImpl])

  private[server] def isLeafTrie(trie: OntologyTrie): Boolean = trie.children.isEmpty

  private[server] def toConcept(path: String) = Concept(path, None)

  private[server] def toTerm(concept: Concept) = new Term(concept.path, concept.category, determineSimpleNameFor(concept))

  private[server] def determineSimpleNameFor(concept: Concept): String = {
    val simpleName = concept.simpleName

    //Return the synonym as the simple name if the simple name is a coded value and a synonym is present; 
    //otherwise, return the simple name unaltered.
    //Useful for medications, which have all-digit simple names, but human-readable synonyms
    if (isCodedValue(concept)) concept.synonym.getOrElse(simpleName) else simpleName
  }

  private[this] val allDigitRegex = """^\d+$""".r

  //Matches things like 'AD100'
  private[this] val medicationCategoryRegex = """^\w\w\d\d\d$""".r

  private[server] def isCodedValue(concept: Concept): Boolean = {
    def simpleNameMatches(regex: Regex) = regex.findFirstIn(concept.simpleName).isDefined

    def isAllDigits = simpleNameMatches(allDigitRegex)

    def isMedicationCategory = simpleNameMatches(medicationCategoryRegex)

    def isLab = concept.category == "Labs"

    isAllDigits || isMedicationCategory || isLab
  }

  private def ontFileStream: java.io.InputStream = {
    val ontFileName = "ShrineWithSyns.sql"

    val stream = getClass.getClassLoader.getResourceAsStream(ontFileName)

    require(stream != null, "Couldn't find ontology file '" + ontFileName + "' on the classpath")

    stream
  }
}
