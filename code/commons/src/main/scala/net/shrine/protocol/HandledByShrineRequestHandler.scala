package net.shrine.protocol

/**
 * @author clint
 * @date Apr 1, 2013
 */
abstract class HandledByShrineRequestHandler(
  override val projectId: String,
  override val waitTimeMs: Long,
  override val authn: AuthenticationInfo) extends DoubleDispatchingShrineRequest(projectId, waitTimeMs, authn) {
  
  override type Handler = ShrineRequestHandler
  
  override val handlerClass = classOf[ShrineRequestHandler]
}
