package net.shrine.config;

import org.spin.tools.config.AgentConfig;
import org.spin.tools.config.EndpointConfig;
import org.spin.tools.config.EndpointType;
import org.spin.tools.config.KeyStoreConfig;
import org.spin.tools.config.NodeConfig;
import org.spin.tools.config.RoutingTableConfig;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * ShrineConfig provides configuration details for broadcaster-aggregator and adapter configurations.
 * ShrineConfig is the "minimal describes the amount of information necessary to configure a shrine server" in one convenient location.
 * ShrineConfig is intended to be used in combination with I2B2HiveCredentials on startup to minimize duplicating configuration settings.
 *
 * @author Andrew McMurry, MS
 * @date Jan 6, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 * @see net.shrine.config.I2B2HiveCredentials
 *      <p/>
 *      Under the hood, ShrineConfig can be used to populate configuration objects as needed
 * @see AgentConfig
 * @see NodeConfig
 * @see KeyStoreConfig
 */
@XmlRootElement(name = "shrineConfig")
public class ShrineConfig {
    /**
     * Human readable name shown to actual users, NOT a servername!
     * remove "hostnames.properties" from existence and fix this in SPIN base.
     * TODO http://jira.open.med.harvard.edu/browse/BASE-373
     */
    protected String humanReadableNodeName;

    /**
     * Behave as a broadcaster-Aggregator.
     * Read architecture specification to learn more about this behavior.
     *
     * @see #broadcasterPeerGroupToQuery
     * @see #queryTTL
     * @see #cacheTTL
     * @see #certificationTTL
     */
    protected boolean isBroadcasterAggregator = true;

    /**
     * Many routing tables can be configured for a single node.
     * This is the peer group to query by default.
     *
     * @see RoutingTableConfig
     * @deprecated TODO http://jira.open.med.harvard.edu/browse/SHRINE-456
     */
    protected String broadcasterPeerGroupToQuery;

    /**
     * Client (spin agent) Query TTL when calling agent.receive(...)
     * Defaults to 3 minutes to conincide with the i2b2 default.
     * This is an int to conform to RequestHeaderType.setResultWaittimeMs
     * Ignored when isBroadcasterAggregator=false
     */
    protected int queryTTL = 180 * 1000;

    /**
     * Broadcaster-Aggregator Cache TTL. The cache is actually backed by the MemoryResidentCache in the SPIN library.
     * The default is 1 hour to coincide with a maximum reasonable single user session.
     * this property is ignored when isBroadcasterAggregator=false
     */
    protected long cacheTTL = 60 * 60 * 1000;

    /**
     * The default is 1 hour to coincide with a maximum reasonable single user session.
     * This property is used by both the Adapter and Broadcaster.
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
     * default 7 attempts returning the same exact number of patients
     * this property is ignored when isAdapter=false
     */
    protected Integer adapterLockoutAttemptsThreshold = 7;

    protected boolean setSizeObfuscationEnabled = true;

    /**
     * Require adapter mappings to exist for each-and-every item Such as \\SHRINE\Demographics\Age\0\
     * Default to TRUE (fail fast on queries that we dont have answers for).
     * this property is ignored when isAdapter=false
     */
    protected boolean adapterRequireExplicitMappings = true;

    /**
     * The implementation fo the identity service that decouples the broadcaster-aggregator from PM specific details.
     * Furthermore, this could refer to any implementation such as the 60 site CarraNet which may have different PM setup.
     */
    //TODO remove me - this should be handled by spring now
    protected String identityServiceClass = null;

    /**
     * Properties file to bootstrap Ibatis configuration, see jdbc-derby.properties
     */
    protected String databasePropertiesFile;

    /**
     * The endpoint of the real CRC.  This is consumed by the Adapter, which actually needs to talk to
     * i2b2 to fetch data.
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

    //TODO remove me - this should be handled by spring now
    protected String translatorClass;
    private String queryActionMapClassName;

    private String adapterStatusQuery;

    private Boolean includeAggregateResult;

    public Boolean isIncludeAggregateResult() {
        return includeAggregateResult;
    }

    public void setIncludeAggregateResult(Boolean includeAggregateResult) {
        this.includeAggregateResult = includeAggregateResult;
    }

    public String getAdapterStatusQuery() {
        return adapterStatusQuery;
    }

    public void setAdapterStatusQuery(String adapterStatusQuery) {
        this.adapterStatusQuery = adapterStatusQuery;
    }

    public boolean isSetSizeObfuscationEnabled() {
        return setSizeObfuscationEnabled;
    }

    public void setSetSizeObfuscationEnabled(boolean setSizeObfuscationEnabled) {
        this.setSizeObfuscationEnabled = setSizeObfuscationEnabled;
    }

    public String getShrineEndpoint() {
        return shrineEndpoint;
    }

    public void setShrineEndpoint(String shrineEndpoint) {
        this.shrineEndpoint = shrineEndpoint;
    }

    public String getAggregatorEndpoint() {
        return aggregatorEndpoint;
    }

    public void setAggregatorEndpoint(String aggregatorEndpoint) {
        this.aggregatorEndpoint = aggregatorEndpoint;
    }

    public String getPmEndpoint() {
        return pmEndpoint;
    }

    public void setPmEndpoint(String pmEndpoint) {
        this.pmEndpoint = pmEndpoint;
    }

    public String getOntEndpoint() {
        return ontEndpoint;
    }

    public void setOntEndpoint(String ontEndpoint) {
        this.ontEndpoint = ontEndpoint;
    }

    public String getSheriffEndpoint() {
        return sheriffEndpoint;
    }

    public void setSheriffEndpoint(String sheriffEndpoint) {
        this.sheriffEndpoint = sheriffEndpoint;
    }

    public String getDatabasePropertiesFile() {
        return databasePropertiesFile;
    }

    public void setDatabasePropertiesFile(String databasePropertiesFile) {
        this.databasePropertiesFile = databasePropertiesFile;
    }

    public void setAdapterLockoutAttemptsThreshold(
            Integer adapterLockoutAttemptsThreshold) {
        this.adapterLockoutAttemptsThreshold = adapterLockoutAttemptsThreshold;
    }

    public String getHumanReadableNodeName() {
        return humanReadableNodeName;
    }

    public void setHumanReadableNodeName(String humanReadableNodeName) {
        this.humanReadableNodeName = humanReadableNodeName;
    }

    public boolean isBroadcasterAggregator() {
        return isBroadcasterAggregator;
    }

    public void setBroadcasterAggregator(boolean broadcasterAggregator) {
        isBroadcasterAggregator = broadcasterAggregator;
    }

    public String getBroadcasterPeerGroupToQuery() {
        return broadcasterPeerGroupToQuery;
    }

    public void setBroadcasterPeerGroupToQuery(String broadcasterPeerGroupToQuery) {
        this.broadcasterPeerGroupToQuery = broadcasterPeerGroupToQuery;
    }

    public boolean isAdapter() {
        return isAdapter;
    }

    public void setAdapter(boolean adapter) {
        isAdapter = adapter;
    }

    public int getQueryTTL() {
        return queryTTL;
    }

    public void setQueryTTL(int queryTTL) {
        this.queryTTL = queryTTL;
    }

    public long getCacheTTL() {
        return cacheTTL;
    }

    public void setCacheTTL(long cacheTTL) {
        this.cacheTTL = cacheTTL;
    }

    public long getCertificationTTL() {
        return certificationTTL;
    }

    public void setCertificationTTL(long certificationTTL) {
        this.certificationTTL = certificationTTL;
    }

    public int getAdapterLockoutAttemptsThreshold() {
        return adapterLockoutAttemptsThreshold;
    }

    public void setAdapterLockoutAttemptsThreshold(int adapterLockoutAttemptsThreshold) {
        this.adapterLockoutAttemptsThreshold = adapterLockoutAttemptsThreshold;
    }

    public boolean isAdapterRequireExplicitMappings() {
        return adapterRequireExplicitMappings;
    }

    public void setAdapterRequireExplicitMappings(boolean adapterRequireExplicitMappings) {
        this.adapterRequireExplicitMappings = adapterRequireExplicitMappings;
    }

    public String getIdentityServiceClass() {
        return identityServiceClass;
    }

    public void setIdentityServiceClass(String identityServiceClass) {
        this.identityServiceClass = identityServiceClass;
    }

    public String getRealCRCEndpoint() {
        return realCRCEndpoint;
    }

    public void setRealCRCEndpoint(String realCRCEndpoint) {
        this.realCRCEndpoint = realCRCEndpoint;
    }

    public String getTranslatorClass() {
        return translatorClass;
    }

    public void setTranslatorClass(String translatorClass) {
        this.translatorClass = translatorClass;
    }

    public String getQueryActionMapClassName() {
        return queryActionMapClassName;
    }

    public void setQueryActionMapClassName(String queryActionMapClassName) {
        this.queryActionMapClassName = queryActionMapClassName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ShrineConfig that = (ShrineConfig) o;

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
        result = 31 * result + (int) (cacheTTL ^ (cacheTTL >>> 32));
        result = 31 * result + (int) (certificationTTL ^ (certificationTTL >>> 32));
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
        return "ShrineConfig{" +
                "humanReadableNodeName='" + humanReadableNodeName + '\'' +
                ", isBroadcasterAggregator=" + isBroadcasterAggregator +
                ", broadcasterPeerGroupToQuery='" + broadcasterPeerGroupToQuery + '\'' +
                ", queryTTL=" + queryTTL +
                ", cacheTTL=" + cacheTTL +
                ", certificationTTL=" + certificationTTL +
                ", isAdapter=" + isAdapter +
                ", adapterLockoutAttemptsThreshold=" + adapterLockoutAttemptsThreshold +
                ", adapterRequireExplicitMappings=" + adapterRequireExplicitMappings +
                ", translatorClass=" + translatorClass +
                '}';
    }

    public AgentConfig generateAgentConfig() {
        EndpointConfig endpointConfig = new EndpointConfig(EndpointType.SOAP, aggregatorEndpoint);
        AgentConfig agentConfig = new AgentConfig(endpointConfig, endpointConfig,
                broadcasterPeerGroupToQuery, null, new Long(queryTTL), 1.0f);


        return agentConfig;
    }

    public NodeConfig generateNodeConfig() {
        NodeConfig nodeConfig = new NodeConfig(this.getHumanReadableNodeName(),
                false,
                this.isBroadcasterAggregator(),
                this.isBroadcasterAggregator(),
                this.isAdapter,
                this.getIdentityServiceClass(),
                this.getCertificationTTL(),
                this.getCacheTTL(),
                NodeConfig.defaultBroadcastTimeoutPeriod,
                NodeConfig.defaultResultStoreType,
                queryActionMapClassName,
                null);
        return nodeConfig;
    }
}
