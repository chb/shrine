package net.shrine.webclient.server

import net.shrine.webclient.shared.domain.MultiInstitutionQueryResult

/**
 * @author Clint
 * @date Aug 3, 2012
 */
trait QueryService {
  def performQuery(expr: String): MultiInstitutionQueryResult
}