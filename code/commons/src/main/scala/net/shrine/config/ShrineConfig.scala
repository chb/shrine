package net.shrine.config

import org.spin.tools.config.AgentConfig
import org.spin.tools.config.EndpointConfig
import org.spin.tools.config.KeyStoreConfig
import org.spin.tools.config.NodeConfig
import org.spin.tools.config.RoutingTableConfig
import org.spin.tools.config.QueryTypeConfig

/**
 * ShrineConfig provides configuration details for broadcaster-aggregator and
 * adapter configurations. ShrineConfig is the
 * "minimal describes the amount of information necessary to configure a shrine server"
 * in one convenient location. ShrineConfig is intended to be used in
 * combination with I2B2HiveCredentials on startup to minimize duplicating
 * configuration settings.
 * 
 * @author Andrew McMurry, MS
 * @date Jan 6, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html Under the hood, ShrineConfig can
 *       be used to populate configuration objects as needed
 * @see AgentConfig
 * @see NodeConfig
 * @see KeyStoreConfig
 */
final case class ShrineConfig(
    /**
     * Human readable name shown to actual users, NOT a servername! remove
     * "hostnames.properties" from existence and fix this in SPIN base. TODO
     * http://jira.open.med.harvard.edu/browse/BASE-373
     */
    var humanReadableNodeName: String,

    /**
     * Behave as a broadcaster-Aggregator. Read architecture specification to
     * learn more about this behavior.
     * 
     * @see #broadcasterPeerGroupToQuery
     * @see #queryTTL
     * @see #cacheTTL
     * @see #certificationTTL
     */
    var isBroadcasterAggregator: Boolean,// = true

    /**
     * Many routing tables can be configured for a single node. This is the peer
     * group to query by default.
     * 
     * @see RoutingTableConfig
     * @deprecated TODO http://jira.open.med.harvard.edu/browse/SHRINE-456
     */
    @Deprecated
    var broadcasterPeerGroupToQuery: String,

    /**
     * Client (spin agent) Query TTL when calling agent.receive(...) Defaults to
     * 3 minutes to conincide with the i2b2 default. This is an int to conform
     * to RequestHeaderType.setResultWaittimeMs Ignored when
     * isBroadcasterAggregator=false
     */
    var queryTTL: Int,// = 180 * 1000

    /**
     * Broadcaster-Aggregator Cache TTL. The cache is actually backed by the
     * MemoryResidentCache in the SPIN library. The default is 1 hour to
     * coincide with a maximum reasonable single user session. this property is
     * ignored when isBroadcasterAggregator=false
     */
    var cacheTTL: Long,// = 60 * 60 * 1000

    /**
     * The default is 1 hour to coincide with a maximum reasonable single user
     * session. This property is used by both the Adapter and Broadcaster.
     */
    var certificationTTL: Long,// =  60 * 60 * 1000,

    /**
     * Behave as an Adapter
     * 
     * @see #adapterLockoutAttemptsThreshold
     * @see #adapterRequireExplicitMappings
     */
    var isAdapter: Boolean,// = true

    /**
     * Lockout a #specific# user after numerous attempts at the same query
     * default 7 attempts returning the same exact number of patients this
     * property is ignored when isAdapter=false
     */
    var adapterLockoutAttemptsThreshold: Int,// = 7

    var setSizeObfuscationEnabled: Boolean,// = true

    /**
     * Require adapter mappings to exist for each-and-every item Such as
     * \\SHRINE\Demographics\Age\0\ Default to TRUE (fail fast on queries that
     * we dont have answers for). this property is ignored when isAdapter=false
     */
    var adapterRequireExplicitMappings: Boolean,// = true

    /**
     * The implementation fo the identity service that decouples the
     * broadcaster-aggregator from PM specific details. Furthermore, this could
     * refer to any implementation such as the 60 site CarraNet which may have
     * different PM setup.
     */
    // TODO remove me - this should be handled by spring now
    var identityServiceClass: String,

    /**
     * Properties file to bootstrap Ibatis configuration, see
     * jdbc-derby.properties
     */
    var databasePropertiesFile: String,

    /**
     * The endpoint of the real CRC. This is consumed by the Adapter, which
     * actually needs to talk to i2b2 to fetch data.
     * <p/>
     * Url should look something like "http://host:port/path"
     */
    var realCRCEndpoint: String,

    /**
     * This is the endpoint of us, the shrine endpoint emulating a CRC
     */
    var shrineEndpoint: String,

    var aggregatorEndpoint: String,
    var pmEndpoint: String,
    var ontEndpoint: String,
    var sheriffEndpoint: String,

    // TODO remove me - this should be handled by spring now
    var translatorClass: String,
    var queryActionMapClassName: String,

    var adapterStatusQuery: String,

    var includeAggregateResult: Boolean) {

    //NB: For tests
    //ShrineConfig(final String humanReadableNodeName, final boolean isBroadcasterAggregator, final String broadcasterPeerGroupToQuery, final int queryTTL, final long cacheTTL, final long certificationTTL, final boolean isAdapter, final Integer adapterLockoutAttemptsThreshold, final boolean setSizeObfuscationEnabled, final boolean adapterRequireExplicitMappings, final String identityServiceClass, final String databasePropertiesFile, final String realCRCEndpoint, final String shrineEndpoint, final String aggregatorEndpoint, final String pmEndpoint, final String ontEndpoint, final String sheriffEndpoint, final String translatorClass, final String queryActionMapClassName, final String adapterStatusQuery, final Boolean includeAggregateResult) {

  def this() = this(null, true, null, 180 * 1000,  60 * 60 * 1000,  60 * 60 * 1000, true, 7, true, true, null, null, null, null, null, null, null, null, null, null, null, false)
  
  def generateAgentConfig: AgentConfig = {
    val endpoint = EndpointConfig.soap(aggregatorEndpoint)

    val rootAggregatorEndpoint = endpoint
    val entryPoint = endpoint
    val authEndpoint = null
    val pollingFrequency = 1.0F
    
    new AgentConfig(rootAggregatorEndpoint, entryPoint, broadcasterPeerGroupToQuery, authEndpoint, queryTTL, pollingFrequency)
  }

  def generateNodeConfig: NodeConfig = {
    val isAuthenticator = false
    
    val queries: java.util.Collection[QueryTypeConfig] = null
    
    new NodeConfig(humanReadableNodeName, isAuthenticator, isBroadcasterAggregator, isBroadcasterAggregator, isAdapter, identityServiceClass, certificationTTL, cacheTTL, NodeConfig.defaultBroadcastTimeoutPeriod, queryActionMapClassName, queries)
  }
}
