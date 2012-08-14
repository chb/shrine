package net.shrine.ont.index

import org.apache.lucene.util.Version
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.store._
import net.shrine.ont.messaging.Concept
import scala.collection.mutable.HashMap
import org.apache.lucene.index._
import net.shrine.ont.data.OntologyDAO
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.document.{ Field, Document }
import org.apache.lucene.search.{ TermQuery, TopDocs, IndexSearcher }
import org.apache.lucene.search.ScoreDoc
import net.shrine.ont.data.ShrineSqlOntologyDAO
import org.apache.lucene.queryParser.MultiFieldQueryParser
import scala.collection.mutable.ArrayBuffer

/**
 * @author Dave Ortiz
 * @author Clint Gilbert
 * @date 9/14/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class LuceneOntologyIndex(ontologyDao: OntologyDAO, dirBuilder: OntologyDAO => IndexWriter => Directory) extends OntologyIndex {
  private val analyzer = new StandardAnalyzer(LuceneOntologyIndex.luceneVersion)

  private val searchDirectory = {

    val dir = new RAMDirectory

    val writer = new IndexWriter(dir, analyzer, IndexWriter.MaxFieldLength.UNLIMITED)

    dirBuilder(ontologyDao)(writer)
  }

  private val searcher = new IndexSearcher(searchDirectory)

  import LuceneOntologyIndex.Keys
  
  private val parser: QueryParser = {
    import scala.collection.JavaConverters._
    import java.util.{Map => JMap}
    import java.lang.{Float => JFloat}
    
    //TODO: Evaluate if these boosts are appropriate.  Ranking is ok for now.
    val boosts: JMap[String, JFloat] = Map(Keys.Path -> 2.0F, Keys.SimpleName -> 25.0F).mapValues(JFloat.valueOf).asJava
    
    val result = new MultiFieldQueryParser(LuceneOntologyIndex.luceneVersion, Array(Keys.Path, Keys.Synonym, Keys.SimpleName, Keys.BaseCode), analyzer, boosts)
    
    result.setDefaultOperator(QueryParser.Operator.AND)
    
    //NB: Allow leading-wildcard queries: ie *FOO* would match 123asdasdFOOaklsdjksal
    //This might cause performance problems, but seems ok for now
    result.setAllowLeadingWildcard(true);
    
    result
  }

  //FIXME: Enforces wildcard matching by surrounding all terms with '*'s.
  //Leading-wildcard queries might cause performance problems, but it seems ok for now. 
  private def munge(query: String): String = {
    query.split("""\s+""").map {
      case and @ "AND" => and
      case t if t != "" => "*" + t + "*"
    }.mkString(" ")
  }

  private def removeDupes[T, F](ts: Seq[T])(field: T => F): Seq[T] = {
    val buffer = new ArrayBuffer[T]
    val iter = ts.iterator
    var seenBefore = Set.empty[F]
    while (iter.hasNext) {
      val o = iter.next
      val f = field(o)
      if (!seenBefore(f)) {
        seenBefore += f
        buffer += o
      }
    }

    buffer.toSeq
  }
  
  /**
   * Pass a search query (a bunch of terms)
   */
  override def search(queryString: String): Seq[Concept] = {
    def toConcept(scoreDoc: ScoreDoc): Concept = {
      val doc = searcher.doc(scoreDoc.doc)

      Concept(doc.get(Keys.Path), Option(doc.get(Keys.Synonym)), Option(doc.get(Keys.BaseCode)))
    }

    val mungedQueryString = munge(queryString)

    val query = parser.parse(mungedQueryString)

    val topDocs = searcher.search(query, 1000)

    val resultsBestToWorst = topDocs.scoreDocs.sortBy(-_.score).map(toConcept)
    
    removeDupes(resultsBestToWorst)(_.path)
  }

  override def shutDown() {
    searcher.close()
    searchDirectory.close()
  }
}

object LuceneOntologyIndex {
  def apply(ontologyDao: OntologyDAO) = new LuceneOntologyIndex(ontologyDao, defaultBuilder)

  object Keys {
    val Level = "level"
    val Path = "key"
    val Synonym = "synonym"
    val Category = "category"
    val SimpleName = "simpleName"
    val BaseCode = "baseCode"
  }

  val luceneVersion = Version.LUCENE_34

  def defaultBuilder(dao: OntologyDAO)(writer: IndexWriter): Directory = {
    def withWriter[T](writer: IndexWriter)(f: => T): T = { try { f } finally { writer.close() } }

    withWriter(writer) {
      val conceptDocs = dao.ontologyEntries.map { entry =>
        val conceptDoc = new Document

        def field(name: String, value: String) = new Field(name, value, Field.Store.YES, Field.Index.ANALYZED)

        conceptDoc.add(field(Keys.Path, entry.path))

        for (synonym <- entry.synonym) {
          conceptDoc.add(field(Keys.Synonym, synonym))
        }
        
        for (baseCode <- entry.baseCode) {
          conceptDoc.add(field(Keys.BaseCode, baseCode))
        }
        
        conceptDoc.add(field(Keys.SimpleName, entry.simpleName))

        conceptDoc
      }

      conceptDocs.foreach(writer.addDocument)

      writer.getDirectory
    }
  }

  def main(args: Array[String]) {
    def prompt() = print("> ")

    val in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in))

    val dao = new ShrineSqlOntologyDAO(new java.io.FileInputStream("/home/clint/workspace/shrine-trunk/ontology/core/ShrineWithSyns.sql"))

    val index = LuceneOntologyIndex(dao)

    prompt()

    var line = in.readLine

    while (line != null) {
      if (line.trim != "") {
        val concepts = index.search(line.trim)

        concepts.foreach(println)

        println(concepts.size + " results")
      }

      prompt()

      line = in.readLine
    }
  }
}
