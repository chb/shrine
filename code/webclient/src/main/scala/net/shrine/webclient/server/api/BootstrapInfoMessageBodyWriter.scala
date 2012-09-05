package net.shrine.webclient.server.api

import javax.ws.rs.ext.Provider
import java.io.OutputStream
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.MediaType
import java.lang.reflect.Type
import java.lang.annotation.{ Annotation => JAnnotation }
import net.shrine.webclient.shared.domain.BootstrapInfo

@Provider
final class BootstrapInfoMessageBodyWriter extends SingleObjectJsonMessageBodyWriter[BootstrapInfo]