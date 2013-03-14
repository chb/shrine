package net.shrine.broadcaster.dao.slick.tables

import scala.slick.driver.ExtendedProfile
import net.shrine.dao.slick.tables.HasDriver

/**
 * @author clint
 * @date Jan 25, 2013
 */
final class Tables(override val driver: ExtendedProfile) extends HasDriver with AuditEntryComponent