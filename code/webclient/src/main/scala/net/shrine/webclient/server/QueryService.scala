package net.shrine.webclient.server

/**
 * @author Clint
 * @date Aug 3, 2012
 */
trait QueryService {
  def queryForBreakdown(expr: String): MultiInstitutionQueryResult
}