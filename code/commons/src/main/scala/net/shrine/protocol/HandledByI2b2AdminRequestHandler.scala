package net.shrine.protocol

/**
 * @author clint
 * @date Apr 3, 2013
 */
abstract class HandledByI2b2AdminRequestHandler(
  override val projectId: String,
  override val waitTimeMs: Long,
  override val authn: AuthenticationInfo) extends DoubleDispatchingShrineRequest(projectId, waitTimeMs, authn) {
  
  override type Handler = I2b2AdminRequestHandler
}
