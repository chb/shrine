package net.shrine.ont.index

import net.shrine.ont.data.OntologyDAO
import net.shrine.ont.data.ShrineSqlOntologyDAO

/**
 * @author Clint Gilbert
 * @date Apr 2, 2012
 * 
 * TODO: desperately needs unit test
 */
object OntologyTrie {
  def apply(dao: OntologyDAO): OntologyTrie = {
    val result = empty 
    
    result ++= dao.ontologyEntries.map(_.path)
    
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
    val dao: OntologyDAO = new ShrineSqlOntologyDAO(new java.io.FileInputStream("/home/clint/workspace-3.6.2/shrine-gwt-webclient/ontology/core/ShrineWithSyns.sql"))
    
    val trie = OntologyTrie(dao)
    
    val demoTerm = """\\SHRINE\SHRINE\Demographics\Gender\Male\"""
    
    val subTrie = trie.subTrieForPrefix(demoTerm)
      
    val children = subTrie.children
    
    println("Contains? " + trie.contains(demoTerm) + " " + subTrie)
    
    println("Root: " + subTrie.head)
    
    println("Children: " + children)
    
    println("isLeaf? " + children.isEmpty)
    
    /*children.foreach { term =>
      println("Child " + term)
    }*/
  }
}

/**
 * @author Clint Gilbert
 * @date Apr 2, 2012
 */
sealed class OntologyTrie(entries: PrefixMap[java.lang.Boolean]) extends Iterable[String] {

  import OntologyTrie._

  protected def toTerm(term: String): String = {
    addLeadingSlashesIfNeeded(addTrailingSlashIfNeeded(term))
  }
  
  def ++=(terms: Iterable[String]): this.type = {
    terms.foreach(this.+=)
    
    this
  }

  def +=(term: String): this.type = {
    entries += (split(term) -> dummy)
  
    this
  }
  
  override def isEmpty = entries.isEmpty
  
  override def size = entries.size
  
  def contains(term: String) = entries.contains(split(term))
  
  def rawChildren: Iterable[String] = entries.childKeys.map(parts => unSplit(parts))
  
  def children: Iterable[String] = rawChildren.map(toTerm)
  
  def subTrieForPrefix(term: String): OntologySubTrie = new OntologySubTrie(term, entries.withPrefix(split(term)))
  
  override def iterator: Iterator[String] = {
    entries.iterator.map { case (parts, _) => toTerm(unSplit(parts)) }
  }
}

final case class OntologySubTrie(val prefix: String, entries: PrefixMap[java.lang.Boolean]) extends OntologyTrie(entries) {
  import OntologyTrie.addTrailingSlashIfNeeded
  
  private def concat(prefix: String, fragment: String): String = addTrailingSlashIfNeeded(prefix) + fragment
  
  override def toTerm(termFragment: String): String = {
    super.toTerm(concat(prefix, termFragment))
  }
  
  override def subTrieForPrefix(term: String): OntologySubTrie = super.subTrieForPrefix(term).copy(prefix = concat(prefix, term))
  
  
}
