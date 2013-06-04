package net.shrine.adapter.dao.squeryl.tables

import org.squeryl.Schema
import net.shrine.adapter.dao.squeryl.SquerylEntryPoint
import net.shrine.adapter.dao.model.squeryl.SquerylShrineError

/**
 * @author clint
 * @date May 22, 2013
 */
trait ErrorResultsComponent extends AbstractTableComponent { self: Schema =>
  import SquerylEntryPoint._
  
  val errorResults = table[SquerylShrineError]("ERROR_RESULT")
  
  declareThat(errorResults) { 
    _.id is (primaryKey, autoIncremented)
  }
}