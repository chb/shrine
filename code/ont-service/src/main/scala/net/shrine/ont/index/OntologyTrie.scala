package net.shrine.ont.index

import net.shrine.ont.data.OntologyDAO
import net.shrine.ont.data.ShrineSqlOntologyDAO
import net.shrine.ont.messaging.Concept

/**
 * @author Clint Gilbert
 * @date Apr 2, 2012
 * 
 * TODO: desperately needs unit test
 */
object OntologyTrie {
  def apply(dao: OntologyDAO): OntologyTrie = {
    val result = empty 
    
    result ++= dao.ontologyEntries
    
    result
  }

  def empty = new OntologyTrie(PrefixMap.empty)
  
  private val forwardSlash = """\"""
  
  def split(term: String): Seq[String] = term.split("""\\+""").filter(!_.isEmpty).toSeq
    
  def unSplit(parts: Seq[String]): String = parts.mkString(forwardSlash)

  def addTrailingSlashIfNeeded(term: String): String = {
    if(term.endsWith(forwardSlash)) term else term + forwardSlash
  }
  
  def addLeadingSlashesIfNeeded(term: String): String = {
    val twoSlashes = forwardSlash + forwardSlash
    
    term match {
      case _ if term.startsWith(twoSlashes) => term
      case _ if term.startsWith(forwardSlash) => twoSlashes + term.drop(1)
      case _ => twoSlashes + term
    }
  }
  
  def addSlashesIfNeeded(term: String) = addLeadingSlashesIfNeeded(addTrailingSlashIfNeeded(term))
}

/**
 * @author Clint Gilbert
 * @date Apr 2, 2012
 */
sealed class OntologyTrie(entries: PrefixMap[Option[String]]) extends Iterable[Concept] {

  import OntologyTrie._

  protected[index] def toPath(term: String) = addSlashesIfNeeded(term)
  
  def ++=(concepts: Iterable[Concept]): this.type = {
    concepts.foreach(this.+=)
    
    this
  }

  def +=(concept: Concept): this.type = {
    entries += (split(concept.path) -> concept.synonym)
  
    this
  }
  
  override def isEmpty = entries.isEmpty
  
  override def size = entries.size
  
  def contains(concept: Concept) = entries.contains(split(concept.path))
  
  private def toOntPath(pathParts: Seq[String]) = toPath(unSplit(pathParts))
  
  def children: Iterable[Concept] = entries.childEntries.map { case (pathParts, synonymOption) => Concept(toOntPath(pathParts), synonymOption) }
  
  def subTrieForPrefix(concept: Concept): OntologySubTrie = new OntologySubTrie(concept.path, entries.withPrefix(split(concept.path)))
  
  override def iterator: Iterator[Concept] = {
    entries.iterator.map { case (pathParts, synonymOption) => Concept(toOntPath(pathParts), synonymOption) }
  }
}

final case class OntologySubTrie(val prefix: String, entries: PrefixMap[Option[String]]) extends OntologyTrie(entries) {
  import OntologyTrie.addTrailingSlashIfNeeded
  
  private def addPrefixTo(fragment: String): String = addTrailingSlashIfNeeded(prefix) + fragment
  
  protected[index] override def toPath(termFragment: String) = {
    super.toPath(addPrefixTo(termFragment))
  }
  
  override def subTrieForPrefix(concept: Concept): OntologySubTrie = super.subTrieForPrefix(concept).copy(prefix = addPrefixTo(concept.path))
}
