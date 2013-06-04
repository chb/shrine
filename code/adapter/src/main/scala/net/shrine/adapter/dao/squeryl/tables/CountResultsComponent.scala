package net.shrine.adapter.dao.squeryl.tables

import org.squeryl.Schema
import net.shrine.adapter.dao.squeryl.SquerylEntryPoint
import net.shrine.adapter.dao.model.CountRow
import net.shrine.adapter.dao.model.squeryl.SquerylCountRow

/**
 * @author clint
 * @date May 22, 2013
 */
trait CountResultsComponent extends AbstractTableComponent { self: Schema =>
  import SquerylEntryPoint._
  
  val countResults = table[SquerylCountRow]("COUNT_RESULT")
  
  declareThat(countResults) { 
    _.id is (primaryKey, autoIncremented)
  }
}