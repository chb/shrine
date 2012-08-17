package net.shrine.protocol

/**
 * @author clint
 * @date Aug 16, 2012
 */
final case class RequestHeader(val projectId: String, val waitTimeMs: Long, val authn: AuthenticationInfo)