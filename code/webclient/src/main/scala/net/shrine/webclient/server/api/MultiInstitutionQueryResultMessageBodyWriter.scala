package net.shrine.webclient.server.api

import javax.ws.rs.ext.Provider
import net.shrine.webclient.shared.domain.MultiInstitutionQueryResult

/**
 * @author clint
 * @date Aug 6, 2012
 * 
 */
@Provider
final class MultiInstitutionQueryResultMessageBodyWriter extends SingleObjectJsonMessageBodyWriter[MultiInstitutionQueryResult]