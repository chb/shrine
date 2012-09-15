package net.shrine.webclient.server.api

import net.shrine.webclient.shared.domain.BootstrapInfo
import net.shrine.webclient.shared.domain.TermSuggestion
import net.shrine.webclient.shared.domain.OntNode
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.liftweb.json.JValue
import net.liftweb.json.JInt
import net.liftweb.json.JArray
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.protocol.ResultOutputType
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult
import net.shrine.webclient.shared.domain.MultiInstitutionQueryResult
import net.shrine.webclient.server.Helpers
import net.shrine.webclient.shared.domain.Breakdown

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

        for(term <- termIsJsonable.fromJson(termJson)) yield new OntNode(term, children.flatMap(fromJson).asJava, isLeaf)
      }
      case _ => None
    }
  }

  import scala.collection.JavaConverters._

  implicit val singleInstitutionQueryResultIsJsonable: Jsonable[SingleInstitutionQueryResult] = new Jsonable[SingleInstitutionQueryResult] {
    import FieldNames.SingleInstitutionQueryResult._

    import FieldNames.Breakdown.Results
    
    //HACK ALERT: Wrap breakdowns in bogus object with single "results" field, to appease RestyGWT
    override def toJson(results: SingleInstitutionQueryResult): JValue = {
      val breakdowns = results.getBreakdowns.asScala.toMap.mapValues { breakdown =>
        breakdown.asMap.asScala.toMap.mapValues(colValue => JInt(colValue.toLong))
      }

      (Count -> JInt(results.getCount)) ~ (Breakdowns -> (Results -> breakdowns.toMap))
    }

    import I2b2ResultEnvelope.Column

    override def fromJson(json: JValue): Option[SingleInstitutionQueryResult] = {
      def breakdownFromJson(breakdownJson: JValue): Option[Breakdown] = breakdownJson match {
        case JObject(List(columns @ _*)) => {
          val breakdownData = columns.collect {
            case JField(name, JInt(value)) => (name, java.lang.Long.valueOf(value.toLong))
          }.toMap
          
          Some(new Breakdown(breakdownData.asJava))
        }
        case _ => None
      }

      json match {
        case JObject(List(
            JField(Count, JInt(count)), 
            JField(Breakdowns, JObject(List(JField(Results, JObject(List(breakdownFields @ _*)))))))) => {
              
          val unmarshalledBreakdowns = Map.empty ++ (for {
            JField(resultType, breakdownJson) <- breakdownFields
            breakdown <- breakdownFromJson(breakdownJson)
          } yield (resultType, breakdown))

          Some(new SingleInstitutionQueryResult(count.toLong, unmarshalledBreakdowns.asJava))
        }
        case _ => None
      }
    }
  }

  implicit val multiInstitutionQueryResultIsJsonable: Jsonable[MultiInstitutionQueryResult] = new Jsonable[MultiInstitutionQueryResult] {
    val singleInstToJson = implicitly[Jsonable[SingleInstitutionQueryResult]].toJson _

    val singleInstFromJson = implicitly[Jsonable[SingleInstitutionQueryResult]].fromJson _

    import FieldNames.MultiInstitutionQueryResult._
    
    //HACK ALERT: Wrap in bogus object with single "results" field, to appease RestyGWT
    override def toJson(results: MultiInstitutionQueryResult): JValue = (Results -> results.asMap.asScala.toMap.mapValues(singleInstToJson))

    override def fromJson(json: JValue): Option[MultiInstitutionQueryResult] = json match {
      case JObject(List(JField(Results, JObject(List(insts @ _*))))) => {
        val resultMap = Map.empty ++ (for {
          JField(instName, instResultJson) <- insts
          instResult <- singleInstFromJson(instResultJson)
        } yield (instName, instResult))

        Some(new MultiInstitutionQueryResult(resultMap.asJava))
      }
      case _ => None
    }
  }

  import net.shrine.webclient.shared.domain.Term

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

  implicit val bootstrapInfoIsJsonable: Jsonable[BootstrapInfo] = new Jsonable[BootstrapInfo] {
    import FieldNames.BootstrapInfo._

    override def toJson(bootstrapInfo: BootstrapInfo): JValue = {
      (LoggedInUsername -> bootstrapInfo.getLoggedInUsername)
    }

    override def fromJson(json: JValue): Option[BootstrapInfo] = json match {
      case JObject(List(JField(LoggedInUsername, JString(username)))) => Some(new BootstrapInfo(username))
      case _ => None
    }
  }

  private def marshal(json: JValue): String = compact(render(json))

  private def unmarshal(jsonString: String): JValue = parse(jsonString)

  private object FieldNames {
    object MultiInstitutionQueryResult {
      val Results = "results"
    }
    
    object Breakdown {
      val Results = "results"
    }
    
    object SingleInstitutionQueryResult {
      val Count = "count"
      val Breakdowns = "breakdowns"
    }

    object BootstrapInfo {
      val LoggedInUsername = "loggedInUsername"
    }

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