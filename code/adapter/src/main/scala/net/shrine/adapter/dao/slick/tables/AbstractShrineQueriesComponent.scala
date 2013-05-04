package net.shrine.adapter.dao.slick.tables

import net.shrine.dao.slick.tables.HasDriver
import net.shrine.protocol.query.Expression
import net.shrine.adapter.dao.model.ShrineQuery

/**
 * @author clint
 * @date May 3, 2013
 */
trait AbstractShrineQueriesComponent extends HasColumns { self: HasDriver =>
  import self.driver.simple._
  
  object TypeMappers {
    implicit val Shrine: TypeMapper[Expression] = {
      MappedTypeMapper.base[Expression, String](
        expr => expr.toXmlString,
        xml => Expression.fromXml(xml).get) //NB: Fail loudly on purpose
    }
  }
  
  abstract class AbstractShrineQueriesTable(implicit exprTypeMapper: TypeMapper[Expression]) extends Table[ShrineQuery]("SHRINE_QUERY") with HasId with HasLocalId with HasUsernameAndDomain with HasCreationDate {
    def localId = localIdColumn[String]
    def networkId = column[Long]("NETWORK_ID", O.NotNull)
    def name = column[String]("QUERY_NAME", O.NotNull)
    def queryExpr = column[Expression]("QUERY_EXPRESSION", O.NotNull)

    def withoutGeneratedColumns = localId ~ networkId ~ name ~ username ~ domain ~ queryExpr
    
    def inserter = withoutGeneratedColumns returning id

    override def * = (id ~: withoutGeneratedColumns) ~ creationDate <> (ShrineQuery, ShrineQuery.unapply _)
  }
}