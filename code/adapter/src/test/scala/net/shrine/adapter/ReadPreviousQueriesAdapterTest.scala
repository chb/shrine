package net.shrine.adapter

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.util.Util
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.query.Term
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.ReadPreviousQueriesResponse
import net.shrine.protocol.QueryMaster
import net.shrine.protocol.ReadPreviousQueriesRequest
import net.shrine.protocol.BroadcastMessage
import net.shrine.adapter.spring.AbstractShrineJUnitSpringTest

/**
 * @author clint
 * @date Oct 30, 2012
 */
final class ReadPreviousQueriesAdapterTest extends AbstractShrineJUnitSpringTest with AdapterDbTest with ShouldMatchersForJUnit {
  @Test
  def testProcessRequest = afterCreatingTables {
    val Seq((masterId1, queryId1, name1, authn1, expr1), (masterId2, queryId2, name2, authn2, expr2)) = (1 to 2).map(i => ("masterid:" + i, i, "query" + i, AuthenticationInfo("some-domain", "user" + i, Credential("salkhfkjas", false)), Term(i.toString)))
    
    val masterId3 = "kalsjdklasdjklasdlkjaldsagtuegthasgf"
    val queryId3 = queryId1 + 42
    
    //2 queries for authn1, 1 for authn2
    dao.insertQuery(masterId1, queryId1, name1, authn1, expr1)
    dao.insertQuery(masterId2, queryId2, name2, authn2, expr2)
    dao.insertQuery(masterId3, queryId3, name1, authn1, expr1)
    
    val adapter = new ReadPreviousQueriesAdapter(dao)
    
    def toIdentity(authn: AuthenticationInfo) = new Identity(authn.domain, authn.username)
    
    def processRequest(identity: Identity, req: ReadPreviousQueriesRequest) = adapter.processRequest(identity, BroadcastMessage(req)).asInstanceOf[ReadPreviousQueriesResponse]
    
    {
      //bogus id
      val bogusDomain = "alskdjlasd"
      val bogusUser = "salkjdlas"

      val req = ReadPreviousQueriesRequest("some-projectId", 1000L, AuthenticationInfo(bogusDomain, bogusUser, Credential("sadasdsad", false)), bogusUser, 5)
        
      val result = processRequest(new Identity(bogusDomain, bogusUser), req)
      
      result.groupId should equal(Some(bogusDomain))
      result.userId should equal(Some(bogusUser))
      result.queryMasters should equal(Nil)
    }
    
    //Should get 2 QueryMasters for authn1
    {
      val req = ReadPreviousQueriesRequest("some-projectId", 1000L, authn1, authn1.username, 5)
      
      val result = processRequest(toIdentity(authn1), req)
      
      result.groupId should equal(Some(authn1.domain))
      result.userId should equal(Some(authn1.username))
      
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
    
    //Should get 1 QueryMaster for authn2
    {
      val req = ReadPreviousQueriesRequest("some-projectId", 1000L, authn1, authn1.username, 5)
      
      val result = processRequest(toIdentity(authn2), req)
      
      result.groupId should equal(Some(authn2.domain))
      result.userId should equal(Some(authn2.username))
      val Seq(queryMaster) = result.queryMasters
      
      queryMaster.queryMasterId should equal(queryId2.toString)
      queryMaster.name should equal(name2)
      queryMaster.userId should equal(authn2.username)
      queryMaster.groupId should equal(authn2.domain)
      queryMaster.createDate should not be(null) // :/
    }
    
    //Limit to fewer prev. queries than are in the DB
    {
      val req = ReadPreviousQueriesRequest("some-projectId", 1000L, authn1, authn1.username, 1)
      
      val result = processRequest(toIdentity(authn1), req)
      
      result.groupId should equal(Some(authn1.domain))
      result.userId should equal(Some(authn1.username))
      
      val Seq(queryMaster1) = result.queryMasters.sortBy(_.queryMasterId)
      
      queryMaster1.queryMasterId should equal(queryId1.toString)
      queryMaster1.name should equal(name1)
      queryMaster1.userId should equal(authn1.username)
      queryMaster1.groupId should equal(authn1.domain)
      queryMaster1.createDate should not be(null) // :/
    }
  }
}