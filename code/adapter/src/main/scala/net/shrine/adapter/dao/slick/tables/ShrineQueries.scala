package net.shrine.adapter.dao.slick.tables

import net.shrine.protocol.query.Expression
import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.dao.slick.tables.HasDriver
import net.shrine.dao.slick.tables.ProjectionHelpers

/**
 * @author clint
 * @date Oct 12, 2012
 *
 * NB: Named 'shrine_query' since 'query' is a reserved word in Oracle's SQL dialect. :/
 */
trait ShrineQueriesComponent extends HasColumns { self: HasDriver =>
  import self.driver.simple._

  object ShrineQueries extends Table[ShrineQuery]("SHRINE_QUERY") with HasId with HasLocalId with HasUsernameAndDomain with HasCreationDate {
    def localId = localIdColumn[String]
    def networkId = column[Long]("NETWORK_ID", O.NotNull)
    def name = column[String]("QUERY_NAME", O.NotNull)
    def queryExpr = column[Expression]("QUERY_EXPRESSION", O.NotNull)

    def withoutGeneratedColumns = localId ~ networkId ~ name ~ username ~ domain ~ queryExpr
    
    def inserter = withoutGeneratedColumns returning id

    import ProjectionHelpers._

    override def * = id ~~ withoutGeneratedColumns ~ creationDate <> (ShrineQuery, ShrineQuery.unapply _)

    private implicit val expression2StringMapper: TypeMapper[Expression] =
      MappedTypeMapper.base[Expression, String](
        expr => expr.toXmlString,
        xml => Expression.fromXml(xml).get) //TODO: What about failures?
  }
}