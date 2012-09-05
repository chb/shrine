package net.shrine.webclient.server.api

import net.shrine.webclient.shared.domain.TermSuggestion
import javax.ws.rs.ext.Provider
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * @author clint
 * @date Aug 3, 2012
 * 
 */
@Provider
final class SuggestionSeqMessageBodyWriter extends JsonSeqMessageBodyWriter[TermSuggestion]