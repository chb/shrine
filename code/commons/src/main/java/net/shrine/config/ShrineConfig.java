package net.shrine.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.spin.tools.config.AgentConfig;
import org.spin.tools.config.EndpointConfig;
import org.spin.tools.config.KeyStoreConfig;
import org.spin.tools.config.NodeConfig;
import org.spin.tools.config.RoutingTableConfig;

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
@XmlRootElement(name = "shrineConfig")
public class ShrineConfig {
    /**
     * Human readable name shown to actual users, NOT a servername! remove
     * "hostnames.properties" from existence and fix this in SPIN base. TODO
     * http://jira.open.med.harvard.edu/browse/BASE-373
     */
    protected String humanReadableNodeName;

    /**
     * Behave as a broadcaster-Aggregator. Read architecture specification to
     * learn more about this behavior.
     * 
     * @see #broadcasterPeerGroupToQuery
     * @see #queryTTL
     * @see #cacheTTL
     * @see #certificationTTL
     */
    protected boolean isBroadcasterAggregator = true;

    /**
     * Many routing tables can be configured for a single node. This is the peer
     * group to query by default.
     * 
     * @see RoutingTableConfig
     * @deprecated TODO http://jira.open.med.harvard.edu/browse/SHRINE-456
     */
    @Deprecated
    protected String broadcasterPeerGroupToQuery;

    /**
     * Client (spin agent) Query TTL when calling agent.receive(...) Defaults to
     * 3 minutes to conincide with the i2b2 default. This is an int to conform
     * to RequestHeaderType.setResultWaittimeMs Ignored when
     * isBroadcasterAggregator=false
     */
    protected int queryTTL = 180 * 1000;

    /**
     * Broadcaster-Aggregator Cache TTL. The cache is actually backed by the
     * MemoryResidentCache in the SPIN library. The default is 1 hour to
     * coincide with a maximum reasonable single user session. this property is
     * ignored when isBroadcasterAggregator=false
     */
    protected long cacheTTL = 60 * 60 * 1000;

    /**
     * The default is 1 hour to coincide with a maximum reasonable single user
     * session. This property is used by both the Adapter and Broadcaster.
     */
    protected long certificationTTL = 60 * 60 * 1000;

    /**
     * Behave as an Adapter
     * 
     * @see #adapterLockoutAttemptsThreshold
     * @see #adapterRequireExplicitMappings
     */
    protected boolean isAdapter = true;

    /**
     * Lockout a #specific# user after numerous attempts at the same query
     * default 7 attempts returning the same exact number of patients this
     * property is ignored when isAdapter=false
     */
    protected Integer adapterLockoutAttemptsThreshold = 7;

    protected boolean setSizeObfuscationEnabled = true;

    /**
     * Require adapter mappings to exist for each-and-every item Such as
     * \\SHRINE\Demographics\Age\0\ Default to TRUE (fail fast on queries that
     * we dont have answers for). this property is ignored when isAdapter=false
     */
    protected boolean adapterRequireExplicitMappings = true;

    /**
     * The implementation fo the identity service that decouples the
     * broadcaster-aggregator from PM specific details. Furthermore, this could
     * refer to any implementation such as the 60 site CarraNet which may have
     * different PM setup.
     */
    // TODO remove me - this should be handled by spring now
    protected String identityServiceClass = null;

    /**
     * Properties file to bootstrap Ibatis configuration, see
     * jdbc-derby.properties
     */
    protected String databasePropertiesFile;

    /**
     * The endpoint of the real CRC. This is consumed by the Adapter, which
     * actually needs to talk to i2b2 to fetch data.
     * <p/>
     * Url should look something like "http://host:port/path"
     */
    protected String realCRCEndpoint;

    /**
     * This is the endpoint of us, the shrine endpoint emulating a CRC
     */
    protected String shrineEndpoint;

    protected String aggregatorEndpoint;
    protected String pmEndpoint;
    protected String ontEndpoint;
    protected String sheriffEndpoint;

    // TODO remove me - this should be handled by spring now
    protected String translatorClass;
    private String queryActionMapClassName;

    private String adapterStatusQuery;

    private Boolean includeAggregateResult;

    public ShrineConfig() {
        super();
    }

    //NB: For tests
    ShrineConfig(final String humanReadableNodeName, final boolean isBroadcasterAggregator, final String broadcasterPeerGroupToQuery, final int queryTTL, final long cacheTTL, final long certificationTTL, final boolean isAdapter, final Integer adapterLockoutAttemptsThreshold, final boolean setSizeObfuscationEnabled, final boolean adapterRequireExplicitMappings, final String identityServiceClass, final String databasePropertiesFile, final String realCRCEndpoint, final String shrineEndpoint, final String aggregatorEndpoint, final String pmEndpoint, final String ontEndpoint, final String sheriffEndpoint, final String translatorClass, final String queryActionMapClassName, final String adapterStatusQuery, final Boolean includeAggregateResult) {
        super();
        this.humanReadableNodeName = humanReadableNodeName;
        this.isBroadcasterAggregator = isBroadcasterAggregator;
        this.broadcasterPeerGroupToQuery = broadcasterPeerGroupToQuery;
        this.queryTTL = queryTTL;
        this.cacheTTL = cacheTTL;
        this.certificationTTL = certificationTTL;
        this.isAdapter = isAdapter;
        this.adapterLockoutAttemptsThreshold = adapterLockoutAttemptsThreshold;
        this.setSizeObfuscationEnabled = setSizeObfuscationEnabled;
        this.adapterRequireExplicitMappings = adapterRequireExplicitMappings;
        this.identityServiceClass = identityServiceClass;
        this.databasePropertiesFile = databasePropertiesFile;
        this.realCRCEndpoint = realCRCEndpoint;
        this.shrineEndpoint = shrineEndpoint;
        this.aggregatorEndpoint = aggregatorEndpoint;
        this.pmEndpoint = pmEndpoint;
        this.ontEndpoint = ontEndpoint;
        this.sheriffEndpoint = sheriffEndpoint;
        this.translatorClass = translatorClass;
        this.queryActionMapClassName = queryActionMapClassName;
        this.adapterStatusQuery = adapterStatusQuery;
        this.includeAggregateResult = includeAggregateResult;
    }

    public Boolean isIncludeAggregateResult() {
        return includeAggregateResult;
    }

    public void setIncludeAggregateResult(final Boolean includeAggregateResult) {
        this.includeAggregateResult = includeAggregateResult;
    }

    public String getAdapterStatusQuery() {
        return adapterStatusQuery;
    }

    public void setAdapterStatusQuery(final String adapterStatusQuery) {
        this.adapterStatusQuery = adapterStatusQuery;
    }

    public boolean isSetSizeObfuscationEnabled() {
        return setSizeObfuscationEnabled;
    }

    public void setSetSizeObfuscationEnabled(final boolean setSizeObfuscationEnabled) {
        this.setSizeObfuscationEnabled = setSizeObfuscationEnabled;
    }

    public String getShrineEndpoint() {
        return shrineEndpoint;
    }

    public void setShrineEndpoint(final String shrineEndpoint) {
        this.shrineEndpoint = shrineEndpoint;
    }

    public String getAggregatorEndpoint() {
        return aggregatorEndpoint;
    }

    public void setAggregatorEndpoint(final String aggregatorEndpoint) {
        this.aggregatorEndpoint = aggregatorEndpoint;
    }

    public String getPmEndpoint() {
        return pmEndpoint;
    }

    public void setPmEndpoint(final String pmEndpoint) {
        this.pmEndpoint = pmEndpoint;
    }

    public String getOntEndpoint() {
        return ontEndpoint;
    }

    public void setOntEndpoint(final String ontEndpoint) {
        this.ontEndpoint = ontEndpoint;
    }

    public String getSheriffEndpoint() {
        return sheriffEndpoint;
    }

    public void setSheriffEndpoint(final String sheriffEndpoint) {
        this.sheriffEndpoint = sheriffEndpoint;
    }

    public String getDatabasePropertiesFile() {
        return databasePropertiesFile;
    }

    public void setDatabasePropertiesFile(final String databasePropertiesFile) {
        this.databasePropertiesFile = databasePropertiesFile;
    }

    public void setAdapterLockoutAttemptsThreshold(final Integer adapterLockoutAttemptsThreshold) {
        this.adapterLockoutAttemptsThreshold = adapterLockoutAttemptsThreshold;
    }

    public String getHumanReadableNodeName() {
        return humanReadableNodeName;
    }

    public void setHumanReadableNodeName(final String humanReadableNodeName) {
        this.humanReadableNodeName = humanReadableNodeName;
    }

    public boolean isBroadcasterAggregator() {
        return isBroadcasterAggregator;
    }

    public void setBroadcasterAggregator(final boolean broadcasterAggregator) {
        isBroadcasterAggregator = broadcasterAggregator;
    }

    public String getBroadcasterPeerGroupToQuery() {
        return broadcasterPeerGroupToQuery;
    }

    public void setBroadcasterPeerGroupToQuery(final String broadcasterPeerGroupToQuery) {
        this.broadcasterPeerGroupToQuery = broadcasterPeerGroupToQuery;
    }

    public boolean isAdapter() {
        return isAdapter;
    }

    public void setAdapter(final boolean adapter) {
        isAdapter = adapter;
    }

    public int getQueryTTL() {
        return queryTTL;
    }

    public void setQueryTTL(final int queryTTL) {
        this.queryTTL = queryTTL;
    }

    public long getCacheTTL() {
        return cacheTTL;
    }

    public void setCacheTTL(final long cacheTTL) {
        this.cacheTTL = cacheTTL;
    }

    public long getCertificationTTL() {
        return certificationTTL;
    }

    public void setCertificationTTL(final long certificationTTL) {
        this.certificationTTL = certificationTTL;
    }

    public int getAdapterLockoutAttemptsThreshold() {
        return adapterLockoutAttemptsThreshold;
    }

    public void setAdapterLockoutAttemptsThreshold(final int adapterLockoutAttemptsThreshold) {
        this.adapterLockoutAttemptsThreshold = adapterLockoutAttemptsThreshold;
    }

    public boolean isAdapterRequireExplicitMappings() {
        return adapterRequireExplicitMappings;
    }

    public void setAdapterRequireExplicitMappings(final boolean adapterRequireExplicitMappings) {
        this.adapterRequireExplicitMappings = adapterRequireExplicitMappings;
    }

    public String getIdentityServiceClass() {
        return identityServiceClass;
    }

    public void setIdentityServiceClass(final String identityServiceClass) {
        this.identityServiceClass = identityServiceClass;
    }

    public String getRealCRCEndpoint() {
        return realCRCEndpoint;
    }

    public void setRealCRCEndpoint(final String realCRCEndpoint) {
        this.realCRCEndpoint = realCRCEndpoint;
    }

    public String getTranslatorClass() {
        return translatorClass;
    }

    public void setTranslatorClass(final String translatorClass) {
        this.translatorClass = translatorClass;
    }

    public String getQueryActionMapClassName() {
        return queryActionMapClassName;
    }

    public void setQueryActionMapClassName(final String queryActionMapClassName) {
        this.queryActionMapClassName = queryActionMapClassName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ShrineConfig that = (ShrineConfig) o;

        if (adapterRequireExplicitMappings != that.adapterRequireExplicitMappings) {
            return false;
        }
        if (cacheTTL != that.cacheTTL) {
            return false;
        }
        if (certificationTTL != that.certificationTTL) {
            return false;
        }
        if (isAdapter != that.isAdapter) {
            return false;
        }
        if (isBroadcasterAggregator != that.isBroadcasterAggregator) {
            return false;
        }
        if (queryTTL != that.queryTTL) {
            return false;
        }
        if (adapterLockoutAttemptsThreshold != null ? !adapterLockoutAttemptsThreshold.equals(that.adapterLockoutAttemptsThreshold) : that.adapterLockoutAttemptsThreshold != null) {
            return false;
        }
        if (aggregatorEndpoint != null ? !aggregatorEndpoint.equals(that.aggregatorEndpoint) : that.aggregatorEndpoint != null) {
            return false;
        }
        if (broadcasterPeerGroupToQuery != null ? !broadcasterPeerGroupToQuery.equals(that.broadcasterPeerGroupToQuery) : that.broadcasterPeerGroupToQuery != null) {
            return false;
        }
        if (databasePropertiesFile != null ? !databasePropertiesFile.equals(that.databasePropertiesFile) : that.databasePropertiesFile != null) {
            return false;
        }
        if (humanReadableNodeName != null ? !humanReadableNodeName.equals(that.humanReadableNodeName) : that.humanReadableNodeName != null) {
            return false;
        }
        if (identityServiceClass != null ? !identityServiceClass.equals(that.identityServiceClass) : that.identityServiceClass != null) {
            return false;
        }
        if (ontEndpoint != null ? !ontEndpoint.equals(that.ontEndpoint) : that.ontEndpoint != null) {
            return false;
        }
        if (pmEndpoint != null ? !pmEndpoint.equals(that.pmEndpoint) : that.pmEndpoint != null) {
            return false;
        }
        if (realCRCEndpoint != null ? !realCRCEndpoint.equals(that.realCRCEndpoint) : that.realCRCEndpoint != null) {
            return false;
        }
        if (sheriffEndpoint != null ? !sheriffEndpoint.equals(that.sheriffEndpoint) : that.sheriffEndpoint != null) {
            return false;
        }
        if (shrineEndpoint != null ? !shrineEndpoint.equals(that.shrineEndpoint) : that.shrineEndpoint != null) {
            return false;
        }
        if (translatorClass != null ? !translatorClass.equals(that.translatorClass) : that.translatorClass != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = humanReadableNodeName != null ? humanReadableNodeName.hashCode() : 0;
        result = 31 * result + (isBroadcasterAggregator ? 1 : 0);
        result = 31 * result + (broadcasterPeerGroupToQuery != null ? broadcasterPeerGroupToQuery.hashCode() : 0);
        result = 31 * result + queryTTL;
        result = 31 * result + (int) (cacheTTL ^ cacheTTL >>> 32);
        result = 31 * result + (int) (certificationTTL ^ certificationTTL >>> 32);
        result = 31 * result + (isAdapter ? 1 : 0);
        result = 31 * result + (adapterLockoutAttemptsThreshold != null ? adapterLockoutAttemptsThreshold.hashCode() : 0);
        result = 31 * result + (adapterRequireExplicitMappings ? 1 : 0);
        result = 31 * result + (identityServiceClass != null ? identityServiceClass.hashCode() : 0);
        result = 31 * result + (databasePropertiesFile != null ? databasePropertiesFile.hashCode() : 0);
        result = 31 * result + (realCRCEndpoint != null ? realCRCEndpoint.hashCode() : 0);
        result = 31 * result + (shrineEndpoint != null ? shrineEndpoint.hashCode() : 0);
        result = 31 * result + (aggregatorEndpoint != null ? aggregatorEndpoint.hashCode() : 0);
        result = 31 * result + (pmEndpoint != null ? pmEndpoint.hashCode() : 0);
        result = 31 * result + (ontEndpoint != null ? ontEndpoint.hashCode() : 0);
        result = 31 * result + (sheriffEndpoint != null ? sheriffEndpoint.hashCode() : 0);
        result = 31 * result + (translatorClass != null ? translatorClass.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ShrineConfig{" + "humanReadableNodeName='" + humanReadableNodeName + '\'' + ", isBroadcasterAggregator=" + isBroadcasterAggregator + ", broadcasterPeerGroupToQuery='" + broadcasterPeerGroupToQuery + '\'' + ", queryTTL=" + queryTTL + ", cacheTTL=" + cacheTTL + ", certificationTTL=" + certificationTTL + ", isAdapter=" + isAdapter + ", adapterLockoutAttemptsThreshold=" + adapterLockoutAttemptsThreshold + ", adapterRequireExplicitMappings=" + adapterRequireExplicitMappings + ", translatorClass=" + translatorClass + '}';
    }

    public AgentConfig generateAgentConfig() {
        final EndpointConfig endpointConfig = EndpointConfig.soap(aggregatorEndpoint);

        return new AgentConfig(endpointConfig, endpointConfig, broadcasterPeerGroupToQuery, null, new Long(queryTTL), 1.0f);
    }

    public NodeConfig generateNodeConfig() {
        return new NodeConfig(getHumanReadableNodeName(), false, isBroadcasterAggregator(), isBroadcasterAggregator(), isAdapter, getIdentityServiceClass(), getCertificationTTL(), getCacheTTL(), NodeConfig.defaultBroadcastTimeoutPeriod, queryActionMapClassName, null);
    }
}
