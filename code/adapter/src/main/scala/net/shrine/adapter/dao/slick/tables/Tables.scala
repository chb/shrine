package net.shrine.adapter.dao.slick.tables

import scala.slick.driver.ExtendedProfile
import net.shrine.dao.slick.tables.HasDriver

/**
 * @author clint
 * @date Jan 24, 2013
 */
final class Tables(override val driver: ExtendedProfile) extends 
	HasDriver with 
	BreakdownResultsComponent with 
	CountResultsComponent with 
	ErrorResultsComponent with 
	PrivilegedUsersComponent with 
	QueryResultsComponent with 
	ShrineQueriesComponent