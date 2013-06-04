package net.shrine.adapter.dao.squeryl

import net.shrine.adapter.spring.AbstractShrineJUnitSpringTest
import javax.annotation.Resource
import net.shrine.adapter.dao.AdapterDao
import net.shrine.adapter.dao.squeryl.tables.Tables
import org.squeryl.Table
import org.squeryl.Query
import net.shrine.adapter.dao.model.BreakdownResultRow
import net.shrine.adapter.dao.model.ShrineError
import net.shrine.adapter.dao.model.CountRow
import net.shrine.adapter.dao.model.QueryResultRow
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.dao.squeryl.SquerylInitializer

/**
 * @author clint
 * @date May 24, 2013
 */
trait AbstractSquerylAdapterTest { self: AbstractShrineJUnitSpringTest =>
  @Resource(name = "squerylAdapterDao")
  var dao: AdapterDao = _

  @Resource
  var initializer: SquerylInitializer = _
  
  @Resource
  var tables: Tables = _
  
  import SquerylEntryPoint._
  
  private def allRowsQuery[A, B](table: Table[A])(transform: A => B): Query[B] = from(table)(row => select(transform(row)))
  
  protected lazy val queryRows: Query[ShrineQuery] = allRowsQuery(tables.shrineQueries)(_.toShrineQuery)

  protected lazy val queryResultRows: Query[QueryResultRow] = allRowsQuery(tables.queryResults)(_.toQueryResultRow)

  protected lazy val countResultRows: Query[CountRow] = allRowsQuery(tables.countResults)(_.toCountRow)

  protected lazy val breakdownResultRows: Query[BreakdownResultRow] = allRowsQuery(tables.breakdownResults)(_.toBreakdownResultRow)

  protected lazy val errorResultRows: Query[ShrineError] = allRowsQuery(tables.errorResults)(_.toShrineError)

  protected def list[T](q: Query[T]): Seq[T] = q.toSeq
  
  protected def first[T](q: Query[T]): T = q.single
  
  protected def afterCreatingTables(body: => Any): Unit = afterCreatingTablesReturn(body)
  
  protected def afterCreatingTablesReturn[T](body: => T): T = {
    inTransaction {
      tables.drop
        
      tables.create
        
      body
    }
  }
}