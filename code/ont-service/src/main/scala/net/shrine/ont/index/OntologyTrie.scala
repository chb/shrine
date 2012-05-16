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
  
  private val dummy = java.lang.Boolean.TRUE

  def empty = new OntologyTrie(PrefixMap.empty)
  
  private val forwardSlash = """\"""
  
  def split(term: String): Seq[String] = term.split("""\\+""").filter(!_.isEmpty).toSeq
    
  def unSplit(parts: Seq[String]): String = parts.mkString(forwardSlash)

  def addTrailingSlashIfNeeded(term: String): String = {
    if(term.endsWith(forwardSlash)) term else term + forwardSlash
  }
  
  def addLeadingSlashesIfNeeded(term: String): String = {
    val twoSlashes = forwardSlash + forwardSlash
    
    if(term.startsWith(twoSlashes)) term else twoSlashes + term 
  }
  
  def main(args: Array[String]) {
    /*val dao: OntologyDAO = new ShrineSqlOntologyDAO(new java.io.FileInputStream("/home/clint/workspace-3.6.2/shrine-gwt-webclient/ontology/core/ShrineWithSyns.sql"))
    
    val trie = OntologyTrie(dao)
    
    val demoTerm = Concept("""\\SHRINE\SHRINE\Demographics\""", None)
    
    val subTrie = trie.subTrieForPrefix(demoTerm)
      
    val children = subTrie.children*/
    
    val trie = OntologyTrie.empty
    
    val demoTerm = Concept("""\\SHRINE\SHRINE\Demographics\""", Some("DemographicsSyn"))
    val genderTerm = Concept("""\\SHRINE\SHRINE\Demographics\Gender\""", Some("GenderSyn"))
    val maleTerm = Concept("""\\SHRINE\SHRINE\Demographics\Gender\Male\""", Some("MaleSyn"))
    
    trie ++= Seq(demoTerm, genderTerm, maleTerm)
    
    val subTrie = trie.subTrieForPrefix(demoTerm)
      
    val children = subTrie.children
    
    println("Contains? " + trie.contains(demoTerm) + " " + subTrie)
    
    println("Root: " + subTrie.head)
    
    println("Children: " + children)
    
    println("isLeaf? " + children.isEmpty)
  }
}

/**
 * @author Clint Gilbert
 * @date Apr 2, 2012
 */
sealed class OntologyTrie(entries: PrefixMap[Option[String]]) extends Iterable[Concept] {

  import OntologyTrie._

  protected def toPath(term: String): String = {
    addLeadingSlashesIfNeeded(addTrailingSlashIfNeeded(term))
  }
  
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
  
  def children: Iterable[Concept] = entries.childEntries.map { case (pathParts, synonymOption) => Concept(toPath(unSplit(pathParts)), synonymOption) }
  
  def subTrieForPrefix(concept: Concept): OntologySubTrie = new OntologySubTrie(concept.path, entries.withPrefix(split(concept.path)))
  
  override def iterator: Iterator[Concept] = {
    entries.iterator.map { case (pathParts, synonymOption) => Concept(toPath(unSplit(pathParts)), synonymOption) }
  }
}

final case class OntologySubTrie(val prefix: String, entries: PrefixMap[Option[String]]) extends OntologyTrie(entries) {
  import OntologyTrie.addTrailingSlashIfNeeded
  
  private def addPrefixTo(fragment: String): String = addTrailingSlashIfNeeded(prefix) + fragment
  
  protected override def toPath(termFragment: String): String = {
    super.toPath(addPrefixTo(termFragment))
  }
  
  override def subTrieForPrefix(concept: Concept): OntologySubTrie = super.subTrieForPrefix(concept).copy(prefix = addPrefixTo(concept.path))
}
