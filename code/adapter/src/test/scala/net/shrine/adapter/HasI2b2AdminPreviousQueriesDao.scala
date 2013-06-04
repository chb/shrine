package net.shrine.adapter

import net.shrine.adapter.dao.I2b2AdminPreviousQueriesDao
import net.shrine.adapter.dao.squeryl.AbstractSquerylAdapterTest
import net.shrine.adapter.dao.squeryl.SquerylI2b2AdminPreviousQueriesDao

/**
 * @author clint
 * @date Apr 24, 2013
 */
trait HasI2b2AdminPreviousQueriesDao { self: AbstractSquerylAdapterTest =>
  protected def i2b2AdminDao: I2b2AdminPreviousQueriesDao = new SquerylI2b2AdminPreviousQueriesDao(initializer, tables)
}