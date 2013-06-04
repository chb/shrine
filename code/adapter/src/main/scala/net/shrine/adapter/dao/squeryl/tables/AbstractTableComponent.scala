package net.shrine.adapter.dao.squeryl.tables

import org.squeryl.dsl.ast.BaseColumnAttributeAssignment
import net.shrine.adapter.dao.squeryl.SquerylEntryPoint
import org.squeryl.Table
import org.squeryl.Schema

/**
 * @author clint
 * @date May 22, 2013
 */
trait AbstractTableComponent { self: Schema =>
  protected def declareThat[E](table: Table[E])(statements: (E => BaseColumnAttributeAssignment)*) {
    on(table) { entity =>
      statements.map(statement => statement(entity))
    }
  }
}