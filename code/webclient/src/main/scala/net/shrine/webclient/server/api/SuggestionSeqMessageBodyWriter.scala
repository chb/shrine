package net.shrine.webclient.server.api

import javax.ws.rs.ext.Provider
import net.shrine.webclient.shared.domain.TermSuggestion

/**
 * @author clint
 * @date Aug 3, 2012
 * 
 */
@Provider
final class SuggestionSeqMessageBodyWriter extends JsonSeqMessageBodyWriter[TermSuggestion]