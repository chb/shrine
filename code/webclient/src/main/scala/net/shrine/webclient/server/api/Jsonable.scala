package net.shrine.webclient.server.api

import net.shrine.webclient.client.domain.TermSuggestion
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.shrine.webclient.client.domain.OntNode
import net.shrine.webclient.client.domain.Term
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
}

object Jsonable {
  private def marshal(json: JValue): String = compact(render(json))
  
  implicit val termSuggestionIsJsonable: Jsonable[TermSuggestion] = new Jsonable[TermSuggestion] {
    override def toJson(suggestion: TermSuggestion): JValue = {
      ("path" -> suggestion.getPath) ~
      ("simpleName" -> suggestion.getSimpleName) ~
      ("highlight" -> suggestion.getHighlight) ~
      ("synonym" -> suggestion.getSynonym) ~
      ("category" -> suggestion.getCategory) ~
      ("isLeaf" -> suggestion.isLeaf)
    }
  }
  
  implicit val termIsJsonable: Jsonable[Term] = new Jsonable[Term] {
    override def toJson(term: Term): JValue = {
      ("path" -> term.getPath) ~
      ("category" -> term.getCategory) ~
      ("simpleName" -> term.getSimpleName)
    }
  }
  
  implicit val ontNodeIsJsonable: Jsonable[OntNode] = new Jsonable[OntNode] {
    override def toJson(ontNode: OntNode): JValue = {
      import scala.collection.JavaConverters._
      
      ("term" -> termIsJsonable.toJson(ontNode.toTerm)) ~
      ("isLeaf" -> ontNode.isLeaf) ~
      ("children" -> ontNode.getChildren.asScala.map(toJson))
    }
  }
  
  implicit val multiInstitutionQueryResultIsJsonable: Jsonable[MultiInstitutionQueryResult] = new Jsonable[MultiInstitutionQueryResult] {
    override def toJson(results: MultiInstitutionQueryResult): JValue = {
      JObject(results.map { case (key, value) => JField(key, value) }.toList)
    }
  }
}