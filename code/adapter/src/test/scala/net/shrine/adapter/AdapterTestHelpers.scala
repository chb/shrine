package net.shrine.adapter

import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.QueryMaster
import net.shrine.protocol.query.Term
import net.shrine.util.Util

/**
 * @author clint
 * @date Nov 28, 2012
 */
trait AdapterTestHelpers {
  val queryId = 123
  val localMasterId = "kasjdlsajdklajsdkljasd"
  val bogusQueryId = 999
  
  val masterId1 = "1"
  val masterId2 = "2"

  val networkQueryId1 = 999L
  val networkQueryId2 = 456L

  val queryName1 = "query-name1"
  val queryName2 = "query-name2"

  lazy val queryDef1 = QueryDefinition(queryName1, Term("x"))
  lazy val queryDef2 = QueryDefinition(queryName2, Term("y"))

  val userId = "some-other-user"

  val domain = "Some-other-domain"

  val password = "some-val"

  lazy val authn = AuthenticationInfo(domain, userId, Credential(password, false))
  
  lazy val id = new Identity(domain, userId)

  val projectId = "some-project-id"

  val waitTimeMs = 12345L

  lazy val queryMaster1 = QueryMaster(masterId1, queryName1, userId, domain, Util.now)
}