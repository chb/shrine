package net.shrine.config;

/**
 * REFACTORED
 *
 * @author Andrew McMurry, MS
 * @date Jan 7, 2010 (REFACTORED 1.6.6)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public enum CellNames
{
    /**
     * Project Management Cell:
     * 1. authenticate users and
     * 2. provides a "directory service" of Cell Names-->URLs
      */
    PM,

    /**
     * Ontology, aka "Terminology service"
     * 1. provides information about hierarchical concepts
     * 2. enables code lookuups
     * 3. see i2b2 documentation, many more features.
     */
    ONT,

    //TODO: http://jira.open.med.harvard.edu/browse/SHRINE-437
    // "Listing the broadcaster-aggregator should only be done once, not once for REST and once for SOAP callback"
    
    /**
     * Clinical Research Chart, aka "Clinical Database"
     * 1. Provides the REST interface for i2b2 queries
     * 2. For i2b2 compability, this is also the BROADCASTER REST interface.
     * Concretely, the BROADCASTER impersonates a CRC so that the webclient requires little/no modification.
     */
    CRC,

    /**
     * SHRINE Aggregator packages multiple responses into one response on behalf of the client.
     * This SOAP service uses aggregator/cache functions from the SPIN library.
     */
    AGGREGATOR,

    /**
     * The sheriff service is exposed to the broadcaster and checks that a queryTopic has been approved.
     */
    SHERIFF
}
