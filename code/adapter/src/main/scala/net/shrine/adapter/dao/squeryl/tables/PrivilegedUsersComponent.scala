package net.shrine.adapter.dao.squeryl.tables

import org.squeryl.Schema
import net.shrine.adapter.dao.squeryl.SquerylEntryPoint
import net.shrine.adapter.dao.model.squeryl.SquerylPrivilegedUser

/**
 * @author clint
 * @date May 22, 2013
 */
trait PrivilegedUsersComponent extends AbstractTableComponent { self: Schema =>
  import SquerylEntryPoint._

  val privilegedUsers = table[SquerylPrivilegedUser]("PRIVILEGED_USER")

  declareThat(privilegedUsers) (
    _.id is (primaryKey, autoIncremented),
    user => columns(user.username, user.domain) are (indexed, unique)
  )
}