package net.shrine.adapter.translators

import net.shrine.protocol.query.Expression
import net.shrine.protocol.query.Term
import net.shrine.protocol.query.Not
import net.shrine.protocol.query.Or
import net.shrine.protocol.query.And
import net.shrine.protocol.query.DateBounded
import net.shrine.protocol.query.OccuranceLimited
import net.shrine.config.AdapterMappings
import net.shrine.adapter.AdapterMappingException

/**
 * @author Clint Gilbert
 * @date Feb 29, 2012
 *
 * A class to translate query Expressions from Shrine terms to local terms
 * given an AdapterMappings instance.
 *
 * TODO: Should we still throw when no mapping found, like DefaultConceptTranslator does?
 */
object ExpressionTranslator {
  def throwAdapterMappingException(term: Term) = throw new AdapterMappingException("No local terms mapped to '" + term.value + "'")

  def apply(adapterMappings: AdapterMappings): ExpressionTranslator = {
    apply(adapterMappings, ExpressionTranslator.throwAdapterMappingException)
  }

  def apply(adapterMappings: AdapterMappings,
      onMissingMapping: Term => Expression): ExpressionTranslator = {

    import scala.collection.JavaConverters._

    val mappings = Map.empty ++ (for {
      networkTerm <- adapterMappings.getMappings.asScala
      localTerms = adapterMappings.getMappings(networkTerm).asScala.toSet
    } yield {
      (networkTerm, localTerms)
    })

    new ExpressionTranslator(mappings, onMissingMapping)
  }
}

//NB: Second param allows plugging in different behavior when no mapping is found for a term; default is to throw
final class ExpressionTranslator(
    private[translators] val mappings: Map[String, Set[String]],
    onMissingMapping: Term => Expression = ExpressionTranslator.throwAdapterMappingException) {

  private[translators] def translateTerm(term: Term): Expression = {

    val localTerms = mappings.get(term.value) match {
      case Some(syns) => syns
      case None => Set.empty
    }

    localTerms.size match {
      case 0 => onMissingMapping(term)
      case 1 => if(localTerms.head == term.value) term else Term(localTerms.head)
      case _ => Or(localTerms.map(Term).toSeq: _*)
    }
  }

  def translate(expr: Expression): Expression = expr match {
    case And(exprs @ _*) => And(exprs.map(translate): _*)
    case Or(exprs @ _*) => Or(exprs.map(translate): _*)
    case term: Term => translateTerm(term)
    case not: Not => not.withExpr(translate(not.expr))
    case db: DateBounded => db.withExpr(translate(db.expr))
    case ol: OccuranceLimited => ol.withExpr(translate(ol.expr))
  }
}