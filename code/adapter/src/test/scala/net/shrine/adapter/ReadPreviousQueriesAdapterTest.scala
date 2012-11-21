package net.shrine.adapter

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.util.Util
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import org.springframework.test.AbstractDependencyInjectionSpringContextTests
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.query.Term
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.ReadPreviousQueriesResponse
import net.shrine.protocol.QueryMaster

/**
 * @author clint
 * @date Oct 30, 2012
 */
final class ReadPreviousQueriesAdapterTest extends AbstractDependencyInjectionSpringContextTests with AdapterDbTest with ShouldMatchersForJUnit {
  @Test
  def testProcessRequest {
    val Seq((queryId1, name1, authn1, expr1), (queryId2, name2, authn2, expr2)) = (1 to 2).map(i => (i, "query" + i, AuthenticationInfo("some-domain", "user" + i, Credential("salkhfkjas", false)), Term(i.toString)))
    
    val queryId3 = queryId1 + 42
    
    //2 queries for authn1, 1 for authn2
    dao.insertQuery(queryId1, name1, authn1, expr1)
    dao.insertQuery(queryId2, name2, authn2, expr2)
    dao.insertQuery(queryId3, name1, authn1, expr1)
    
    val adapter = new ReadPreviousQueriesAdapter(dao)
    
    def toIdentity(authn: AuthenticationInfo) = new Identity(authn.domain, authn.username)
    
    //final case class ReadPreviousQueriesResponse(val userId: String, val groupId: String, val queryMasters: Seq[QueryMaster]) extends ShrineResponse {
    
    //ReadPreviousQueriesResponse(identity.getUsername, identity.getDomain, previousQueries.map(_.toQueryMaster))
    
    /*
     * case class QueryMaster (
    val queryMasterId: String,
    val name: String,
    val userId: String,
    val groupId: String,
    val createDate: XMLGregorianCalendar)
     */
    
    /*
    QueryMaster(networkId.toString, name, username, domain, dateCreated)
     */
    
    {
      //bogus id
      val bogusDomain = "alskdjlasd"
      val bogusUser = "salkjdlas"
      
      val result = adapter.processRequest(new Identity(bogusDomain, bogusUser), null).asInstanceOf[ReadPreviousQueriesResponse]
      
      result.groupId should equal(bogusDomain)
      result.userId should equal(bogusUser)
      result.queryMasters should equal(Nil)
    }
    
    {
      val result = adapter.processRequest(toIdentity(authn1), null).asInstanceOf[ReadPreviousQueriesResponse]
      
      result.groupId should equal(authn1.domain)
      result.userId should equal(authn1.username)
      val Seq(queryMaster1, queryMaster2) = result.queryMasters.sortBy(_.queryMasterId)
      
      queryMaster1.queryMasterId should equal(queryId1.toString)
      queryMaster1.name should equal(name1)
      queryMaster1.userId should equal(authn1.username)
      queryMaster1.groupId should equal(authn1.domain)
      queryMaster1.createDate should not be(null) // :/
      
      queryMaster2.queryMasterId should equal(queryId3.toString)
      queryMaster2.name should equal(name1)
      queryMaster2.userId should equal(authn1.username)
      queryMaster2.groupId should equal(authn1.domain)
      queryMaster2.createDate should not be(null) // :/
    }
    
    {
      val result = adapter.processRequest(toIdentity(authn2), null).asInstanceOf[ReadPreviousQueriesResponse]
      
      result.groupId should equal(authn2.domain)
      result.userId should equal(authn2.username)
      val Seq(queryMaster) = result.queryMasters
      
      queryMaster.queryMasterId should equal(queryId2.toString)
      queryMaster.name should equal(name2)
      queryMaster.userId should equal(authn2.username)
      queryMaster.groupId should equal(authn2.domain)
      queryMaster.createDate should not be(null) // :/
    }
  }
}