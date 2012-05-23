package net.shrine.ont.index

import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import junit.framework.TestCase
import org.junit.Test
import net.shrine.ont.data.OntologyDAO
import net.shrine.ont.messaging.Concept

/**
 * @author clint
 * @date May 23, 2012
 * 
 */
object OntologyTrieTest {
  final case class MockOntologyDao(override val ontologyEntries: Iterable[Concept] = Nil) extends OntologyDAO
}

final class OntologyTrieTest extends TestCase with AssertionsForJUnit with ShouldMatchers {
  
  import OntologyTrieTest._
  
  private val rootConcept = Concept("""\\SHRINE\SHRINE\""", Some("Shrine"))
  private val fooConcept = Concept("""\\SHRINE\SHRINE\foo\""", Some("Foo"))
  private val barConcept = Concept("""\\SHRINE\SHRINE\foo\bar\""", Some("Bar"))
  private val bazConcept = Concept("""\\SHRINE\SHRINE\foo\baz\""", Some("Baz"))
  private val blargConcept = Concept("""\\SHRINE\SHRINE\foo\baz\blarg\""", Some("Blarg"))
  private val nuhConcept = Concept("""\\SHRINE\SHRINE\blah\nuh\""", Some("Nuh"))
  
  @Test
  def testApply {
    val empty = OntologyTrie(MockOntologyDao())
    
    empty.size should be(0)
    
    val oneThing = OntologyTrie(MockOntologyDao(Seq(barConcept)))
    
    oneThing.size should be(1)
    
    oneThing.head.path should equal(barConcept.path)
    oneThing.head.synonym should equal(barConcept.synonym)
    
    val twoThings = OntologyTrie(MockOntologyDao(Seq(barConcept, nuhConcept)))
    
    twoThings.size should be(2)
    
    twoThings.head.path should equal(barConcept.path)
    twoThings.head.synonym should equal(barConcept.synonym)
    
    twoThings.tail.head.path should equal(nuhConcept.path)
    twoThings.tail.head.synonym should equal(nuhConcept.synonym)
  }

  @Test
  def testEmpty {
    val empty = OntologyTrie.empty
    
    empty.size should be(0)
    empty.isEmpty should be(true)
    
    (empty eq OntologyTrie.empty) should be(false)
  } 
  
  @Test
  def testSplit {
    import OntologyTrie.split
    
    split("""\\SHRINE\SHRINE\foo\bar\""") should equal(Seq("SHRINE", "SHRINE", "foo", "bar"))
    
    split("""nuh\zuh""") should equal(Seq("nuh", "zuh"))
  }
    
  @Test
  def testUnSplit {
    import OntologyTrie.unSplit
    
    unSplit(Seq("SHRINE", "SHRINE", "foo", "bar")) should equal("""SHRINE\SHRINE\foo\bar""")
  }

  @Test
  def testAddTrailingSlashIfNeeded {
    import OntologyTrie.addTrailingSlashIfNeeded
    
    addTrailingSlashIfNeeded("") should equal("""\""")
    addTrailingSlashIfNeeded("x") should equal("""x\""")
    
    addTrailingSlashIfNeeded("""\""") should equal("""\""")
    addTrailingSlashIfNeeded("""asdf\""") should equal("""asdf\""")
  }
  
  @Test
  def testAddLeadingSlashesIfNeeded {
    import OntologyTrie.addLeadingSlashesIfNeeded
    
    addLeadingSlashesIfNeeded("") should equal("""\\""")
    addLeadingSlashesIfNeeded("x") should equal("""\\x""")
    
    addLeadingSlashesIfNeeded("""\""") should equal("""\\""")
    addLeadingSlashesIfNeeded("""\\""") should equal("""\\""")
    addLeadingSlashesIfNeeded("""\\asdf\""") should equal("""\\asdf\""")
  }
  
  @Test
  def testAddSlashesIfNeeded {
    import OntologyTrie.addSlashesIfNeeded
    
    addSlashesIfNeeded("") should equal("""\\""")
    addSlashesIfNeeded("x") should equal("""\\x\""")
    
    addSlashesIfNeeded("""\""") should equal("""\\""")
    addSlashesIfNeeded("""\\""") should equal("""\\""")
    addSlashesIfNeeded("""\\asdf\""") should equal("""\\asdf\""")
  }
  
  @Test
  def testToPath {
    val trie = OntologyTrie.empty
	
    trie.toPath("") should equal("""\\""")
    trie.toPath("x") should equal("""\\x\""")
    
    trie.toPath("""\""") should equal("""\\""")
    trie.toPath("""\\""") should equal("""\\""")
    trie.toPath("""\\asdf\""") should equal("""\\asdf\""")
  }
  
  @Test
  def testPlusPlusEqual {
    val trie = OntologyTrie.empty
	
	trie.size should be(0)
	
	val newTrie = trie ++= Seq(barConcept, nuhConcept)
    
	(newTrie eq trie) should be(true)
	
	trie.size should be(2)
	
	trie.toSet should equal(Set(barConcept, nuhConcept))
  }

  @Test
  def testPlusEqual {
	val trie = OntologyTrie.empty
	
	trie.size should be(0)
	
	val newTrie = trie += barConcept
    
	(newTrie eq trie) should be(true)
	
	trie.size should be(1)
	
	trie.head should equal(barConcept)
  }
  
  @Test
  def testIsEmpty {
    val trie = OntologyTrie.empty
    
    trie.isEmpty should be(true)
    
    trie += barConcept
    
    trie.isEmpty should be(false)
  }
  
  @Test
  def testSize {
    val trie = OntologyTrie.empty
    
    trie.size should be(0)
    
    trie += barConcept
    
    trie.size should be(1)
    
    trie += nuhConcept
    
    trie.size should be(2)
  }
  
  @Test
  def testContains {
	val trie = OntologyTrie.empty
    
    trie += barConcept
    
    trie += nuhConcept
    
    trie.contains(barConcept) should be(true)
	trie.contains(nuhConcept) should be(true)
	
	trie.contains(fooConcept) should be(false)
  }
  
  @Test
  def testChildren {
    val trie = OntologyTrie.empty
    
    trie ++= Seq(rootConcept, fooConcept, barConcept, bazConcept, blargConcept, nuhConcept)
    
    trie.children.toSeq should equal(Seq(rootConcept))
    
    val fooTrie = trie.subTrieForPrefix(fooConcept)
    
    fooTrie.children.toSeq should equal(Seq(barConcept, bazConcept))
    
    val bazTrie = trie.subTrieForPrefix(bazConcept)
    
    bazTrie.children.toSeq should equal(Seq(blargConcept))
  }
  
  @Test
  def testSubTrieForPrefix {
    val trie = OntologyTrie.empty
    
    trie ++= Seq(rootConcept, fooConcept, barConcept, bazConcept, blargConcept, nuhConcept)
    
    trie.children.toSeq should equal(Seq(rootConcept))
    
    val fooTrie = trie.subTrieForPrefix(fooConcept)
    
    fooTrie.size should equal(4)
    
    fooTrie.children.toSeq should equal(Seq(barConcept, bazConcept))
    
    val bazTrie = trie.subTrieForPrefix(bazConcept)
    
    bazTrie.size should equal(2)
    
    bazTrie.children.toSeq should equal(Seq(blargConcept))
    
    trie.subTrieForPrefix(nuhConcept).size should equal(1)
  }
  
  @Test
  def testIterator {
    OntologyTrie.empty.iterator.toSeq.isEmpty should be(true)
    
    val rootTrie = OntologyTrie.empty += rootConcept
    
    rootTrie.iterator.toSeq should equal(Seq(rootConcept))
    
    val twoThings = OntologyTrie.empty ++= Seq(rootConcept, nuhConcept)
    
    //should be a preorder traversal
    twoThings.iterator.toSeq should equal(Seq(rootConcept, nuhConcept))
    
    val severalThings = OntologyTrie.empty ++= Seq(rootConcept, nuhConcept, blargConcept, fooConcept, barConcept, bazConcept)
    
    //should be a preorder traversal
    severalThings.iterator.toSeq should equal(Seq(rootConcept, nuhConcept, fooConcept, bazConcept, blargConcept, barConcept))
  }
}