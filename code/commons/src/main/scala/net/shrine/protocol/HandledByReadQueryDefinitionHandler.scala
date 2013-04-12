package net.shrine.protocol

import net.shrine.protocol.handlers.ReadQueryDefinitionHandler

/**
 * @author clint
 * @date Apr 11, 2013
 */
abstract class HandledByReadQueryDefinitionHandler(
  override val projectId: String,
  override val waitTimeMs: Long,
  override val authn: AuthenticationInfo) extends DoubleDispatchingShrineRequest(projectId, waitTimeMs, authn) {
  
  override type Handler = ReadQueryDefinitionHandler
  
  override val handlerClass = classOf[ReadQueryDefinitionHandler]
}
