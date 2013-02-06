package net.shrine.config

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import org.spin.tools.JAXBUtils
import org.spin.tools.RandomTool
import org.spin.tools.config.EndpointConfig
import org.spin.tools.config.NodeConfig

/**
 * @author clint
 * @date Sep 6, 2012
 */
final class ShrineConfigTest extends TestCase with AssertionsForJUnit with ShouldMatchers {
  import RandomTool.{ randomInt, randomString }

  val humanReadableNodeName = randomString
  val isBroadcasterAggregator = true
  val broadcasterPeerGroupToQuery = randomString
  val queryTTL = randomInt
  val cacheTTL = randomInt.toLong
  val certificationTTL = randomInt.toLong
  val isAdapter = true
  val adapterLockoutAttemptsThreshold = randomInt
  val setSizeObfuscationEnabled = true
  val adapterRequireExplicitMappings = true
  // TODO remove me - this should be handled by spring now
  val identityServiceClass = randomString
  val databasePropertiesFile = randomString
  val realCRCEndpoint = randomString
  val shrineEndpoint = randomString
  val aggregatorEndpoint = randomString
  val pmEndpoint = randomString
  val ontEndpoint = randomString
  val sheriffEndpoint = randomString
  // TODO remove me - this should be handled by spring now
  val translatorClass = randomString
  val queryActionMapClassName = randomString
  val adapterStatusQuery = randomString
  val includeAggregateResult = true

  val config = new ShrineConfig(humanReadableNodeName, isBroadcasterAggregator, broadcasterPeerGroupToQuery, queryTTL, cacheTTL, certificationTTL, isAdapter, adapterLockoutAttemptsThreshold, 
								 setSizeObfuscationEnabled, adapterRequireExplicitMappings, identityServiceClass, databasePropertiesFile, realCRCEndpoint, shrineEndpoint, aggregatorEndpoint,
								 pmEndpoint, ontEndpoint, sheriffEndpoint, translatorClass, queryActionMapClassName, adapterStatusQuery, includeAggregateResult)
  
  @Test
  def testDefaults {
    val shrineConfig = new ShrineConfig
    
    shrineConfig.isBroadcasterAggregator should be(true)
    shrineConfig.queryTTL should equal(180 * 1000)
    shrineConfig.cacheTTL should equal(60 * 60 * 1000)
    shrineConfig.certificationTTL should equal(60 * 60 * 1000)
    shrineConfig.isAdapter should be(true)
    shrineConfig.adapterLockoutAttemptsThreshold should equal(7)
    shrineConfig.setSizeObfuscationEnabled should be(true)
    shrineConfig.adapterRequireExplicitMappings should be(true)
    shrineConfig.includeAggregateResult should be(false)
  }
  
  @Test
  def testToAgentConfig {
	val agentConfig = config.generateAgentConfig
	
	agentConfig should not be(null)
	agentConfig.getAuthEndpoint should equal(null)
	agentConfig.getNodeConnectorEndpoint should equal(EndpointConfig.soap(aggregatorEndpoint))
	agentConfig.getRootAggregatorEndpoint should equal(EndpointConfig.soap(aggregatorEndpoint))
	agentConfig.getMaxWaitTime should equal(queryTTL)
	agentConfig.getPeerGroupToQuery should equal(broadcasterPeerGroupToQuery)
	agentConfig.getPollingFrequency should equal(1.0F)
  }

  @Test
  def testToNodeConfig {
	val nodeConfig = config.generateNodeConfig
	
	nodeConfig should not be(null)
	
	nodeConfig.getBroadcastTimeoutPeriod should equal(NodeConfig.defaultBroadcastTimeoutPeriod)
	nodeConfig.getCacheTTL should equal(cacheTTL)
	nodeConfig.getCertificationTTL should equal(certificationTTL)
	nodeConfig.getIdentityServiceClass should equal(identityServiceClass)
	nodeConfig.getNodeName should equal(humanReadableNodeName)
	nodeConfig.getQueries.isEmpty should be(true)
	nodeConfig.getQueryActionMapClassName should equal(queryActionMapClassName)
	nodeConfig.isAggregator should equal(isBroadcasterAggregator)
	nodeConfig.isAuthenticator should equal(false)
	nodeConfig.isBroadcaster should equal(isBroadcasterAggregator)
	nodeConfig.isQueryable should equal(isAdapter)
  }
}