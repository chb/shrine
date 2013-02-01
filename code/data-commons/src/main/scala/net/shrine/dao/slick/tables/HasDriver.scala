package net.shrine.dao.slick.tables

import scala.slick.driver.ExtendedProfile

/**
 * @author clint
 * @date Jan 24, 2013
 */
trait HasDriver {
  val driver: ExtendedProfile
}