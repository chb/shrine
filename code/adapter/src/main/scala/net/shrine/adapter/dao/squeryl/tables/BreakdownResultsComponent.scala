package net.shrine.adapter.dao.squeryl.tables

import org.squeryl.Schema
import net.shrine.adapter.dao.model.BreakdownResultRow
import net.shrine.adapter.dao.squeryl.SquerylEntryPoint
import net.shrine.adapter.dao.model.squeryl.SquerylBreakdownResultRow

/**
 * @author clint
 * @date May 22, 2013
 */
trait BreakdownResultsComponent extends AbstractTableComponent { self: Schema =>
  import SquerylEntryPoint._
  
  val breakdownResults = table[SquerylBreakdownResultRow]("BREAKDOWN_RESULT")
  
  declareThat(breakdownResults) { 
    _.id is (primaryKey, autoIncremented)
  }
}