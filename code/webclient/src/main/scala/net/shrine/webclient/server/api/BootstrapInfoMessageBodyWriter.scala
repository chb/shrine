package net.shrine.webclient.server.api

import javax.ws.rs.ext.Provider
import net.shrine.webclient.shared.domain.BootstrapInfo

@Provider
final class BootstrapInfoMessageBodyWriter extends SingleObjectJsonMessageBodyWriter[BootstrapInfo]