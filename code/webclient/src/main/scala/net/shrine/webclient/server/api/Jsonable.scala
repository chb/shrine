package net.shrine.webclient.server.api

import net.shrine.webclient.client.domain.TermSuggestion
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.shrine.webclient.client.domain.OntNode
import net.liftweb.json.JValue
import net.liftweb.json.JInt
import net.shrine.webclient.server.MultiInstitutionQueryResult

/**
 * @author clint
 * @date Aug 3, 2012
 *
 * Type class for things that may be serialized as Json
 */
trait Jsonable[T] {
  def toJson(value: T): JValue

  final def toJsonString(value: T): String = Jsonable.marshal(toJson(value))

  def fromJson(json: JValue): Option[T]

  final def fromJsonString(json: String): Option[T] = fromJson(Jsonable.unmarshal(json))
}

object Jsonable {

  implicit val termSuggestionIsJsonable: Jsonable[TermSuggestion] = new Jsonable[TermSuggestion] {
    import FieldNames.TermSuggestion._
    
    override def toJson(suggestion: TermSuggestion): JValue = {
      (Path -> suggestion.getPath) ~
      (SimpleName -> suggestion.getSimpleName) ~
      (Highlight -> suggestion.getHighlight) ~
      (Synonym -> suggestion.getSynonym) ~
      (Category -> suggestion.getCategory) ~
      (IsLeaf -> suggestion.isLeaf)
    }

    override def fromJson(json: JValue): Option[TermSuggestion] = json match {
      case JObject(List(
        JField(Path, JString(path)), 
        JField(SimpleName, JString(simpleName)), 
        JField(Highlight, JString(highlight)), 
        JField(Synonym, JString(synonym)), 
        JField(Category, JString(category)), 
        JField(IsLeaf, JBool(isLeaf)))) => Some(new TermSuggestion(path, simpleName, highlight, synonym, category, isLeaf))
      case _ => None
    }
  }
  
  implicit val ontNodeIsJsonable: Jsonable[OntNode] = new Jsonable[OntNode] {
    import FieldNames.OntNode._
    
    override def toJson(ontNode: OntNode): JValue = {
      import scala.collection.JavaConverters._

      (Term -> termIsJsonable.toJson(ontNode.toTerm)) ~
      (IsLeaf -> ontNode.isLeaf) ~
      (Children -> ontNode.getChildren.asScala.map(toJson))
    }

    override def fromJson(json: JValue): Option[OntNode] = json match {
      case JObject(List(
          JField(Term, termJson), 
          JField(IsLeaf, JBool(isLeaf)), 
          JField(Children, JArray(List(children @ _*))))) => {
            
        import scala.collection.JavaConverters._
        
        for {
          term <- termIsJsonable.fromJson(termJson)
        } yield {
          new OntNode(term, children.flatMap(fromJson).asJava, isLeaf)
        }
      }
      case _ => None
    }
  }

  implicit val multiInstitutionQueryResultIsJsonable: Jsonable[MultiInstitutionQueryResult] = new Jsonable[MultiInstitutionQueryResult] {
    override def toJson(results: MultiInstitutionQueryResult): JValue = results.toMap

    override def fromJson(json: JValue): Option[MultiInstitutionQueryResult] = json match {
      case JObject(List(fields @ _*)) => {
        val resultMap = fields.collect { case JField(institutionName, JInt(count)) => (institutionName, count.intValue) }.toMap
        
        Some(MultiInstitutionQueryResult(resultMap))
      }
      case _ => None
    }
  }
  
  import net.shrine.webclient.client.domain.Term
  
  implicit val termIsJsonable: Jsonable[Term] = new Jsonable[Term] {
    import FieldNames.Term._
    
    override def toJson(term: Term): JValue = {
      (Path -> term.getPath) ~
      (Category -> term.getCategory) ~
      (SimpleName -> term.getSimpleName)
    }
    
    override def fromJson(json: JValue): Option[Term] = json match {
      case JObject(List(
        JField(Path, JString(path)),
        JField(Category, JString(category)),
        JField(SimpleName, JString(simpleName)))) => Some(new Term(path, category, simpleName))
      case _ => None
    }
  }
  
  private def marshal(json: JValue): String = compact(render(json))

  private def unmarshal(jsonString: String): JValue = parse(jsonString)
  
  private object FieldNames {
    object TermSuggestion {
      val Path = "path"
      val SimpleName = "simpleName"
      val Highlight = "highlight"
      val Synonym = "synonym"
      val Category = "category"
      val IsLeaf = "isLeaf"
    }
    
    object Term {
      val Path = "path"
      val Category = "category"
      val SimpleName = "simpleName"
    }
    
    object OntNode {
      val Term = "term"
      val IsLeaf = "isLeaf"
      val Children = "children"
    }
  }
}