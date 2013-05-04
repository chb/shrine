package net.shrine.adapter.dao.slick.tables

import net.shrine.protocol.query.Expression
import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.dao.slick.tables.HasDriver
import scala.slick.lifted.TypeMapper
import scala.slick.lifted.MappedTypeMapper

/**
 * @author clint
 * @date Oct 12, 2012
 *
 * NB: Named 'shrine_query' since 'query' is a reserved word in Oracle's SQL dialect. :/
 */
trait ShrineQueriesComponent extends AbstractShrineQueriesComponent { self: HasDriver =>
  
  object ShrineQueries extends AbstractShrineQueriesTable()(TypeMappers.Shrine)
  
}

