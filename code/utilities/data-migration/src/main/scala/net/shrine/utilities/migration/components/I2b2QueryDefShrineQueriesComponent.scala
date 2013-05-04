package net.shrine.utilities.migration.components

import net.shrine.adapter.dao.slick.tables.HasColumns
import net.shrine.dao.slick.tables.HasDriver
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.protocol.query.Expression
import net.shrine.adapter.dao.slick.tables.AbstractShrineQueriesComponent
import scala.slick.lifted.TypeMapper
import scala.slick.lifted.MappedTypeMapper
import net.shrine.protocol.query.QueryDefinition

/**
 * @author clint
 * @date May 3, 2013
 *
 * NB: Similar to ShrineQueriesComponent; adapted in order to be able to load
 * values from query_expression column that are present in i2b2-querydef-format as an intermediate
 * step while migrating data from Shrine 1.13 to 1.14
 */
trait I2b2QueryDefShrineQueriesComponent extends AbstractShrineQueriesComponent { self: HasDriver =>

  private val I2b2: TypeMapper[Expression] = {
    MappedTypeMapper.base[Expression, String](
      expr => ???, //NB: read-only, fail loudly on purpose 
      xml => QueryDefinition.fromI2b2(xml).get.expr) //NB: Fail loudly on purpose
  }

  object ShrineQueriesWithI2b2QueryDefs extends AbstractShrineQueriesTable()(I2b2)
}