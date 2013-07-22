package net.shrine.adapter.query;

import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import net.shrine.adapter.dao.DAOException;
import net.shrine.adapter.dao.MasterQueryDefinition;
import net.shrine.adapter.dao.RequestResponseData;
import org.spin.tools.Util;

import java.util.List;

import static org.spin.tools.Util.asSet;

/**
 * @author Justin Quan
 * @version %I% Date: Apr 12, 2010
 */
public abstract class AdapterDAOTest extends AbstractRequestResponseDataTest {


    public void testFindLocalMasterID() throws Exception {
        sanityCheckTestData();

        {
            final String localMasterID = adapterDAO.findLocalMasterID(masterID1);

            assertNotNull(localMasterID);

            assertEquals(localMasterID1, localMasterID);
        }

        {
            final String localMasterID = adapterDAO.findLocalMasterID(masterID2);

            assertNotNull(localMasterID);

            assertEquals(localMasterID2, localMasterID);
        }

        {
            //Look up bogus master ID
            final String localMasterID = adapterDAO.findLocalMasterID(239582L);

            assertNull(localMasterID);
        }
    }

    public void testFindNetworkMasterID() throws Exception {
        sanityCheckTestData();

        {
            final Long networkMasterID = adapterDAO.findNetworkMasterID(localMasterID1);

            assertNotNull(networkMasterID);

            assertEquals(Long.valueOf(masterID1), networkMasterID);
        }

        {
            final Long networkMasterID = adapterDAO.findNetworkMasterID(localMasterID2);

            assertNotNull(networkMasterID);

            assertEquals(Long.valueOf(masterID2), networkMasterID);
        }

        {
            //Look up bogus master ID
            final Long localMasterID = adapterDAO.findNetworkMasterID("239582asklfjlasfj");

            assertNull(localMasterID);
        }
    }

    public void testFindLocalInstanceID() throws Exception {
        sanityCheckTestData();

        {
            final String localInstanceID = adapterDAO.findLocalInstanceID(instanceID1);

            assertNotNull(localInstanceID);

            assertEquals(localInstanceID1, localInstanceID);
        }

        {
            final String localInstanceID = adapterDAO.findLocalInstanceID(instanceID2);

            assertNotNull(localInstanceID);

            assertEquals(localInstanceID2, localInstanceID);
        }

        {
            //Look up bogus master and instance IDs
            final String localInstanceID = adapterDAO.findLocalInstanceID(3847398L);

            assertNull(localInstanceID);
        }
    }

    public void testFindNetworkInstanceID() throws Exception {
        sanityCheckTestData();

        {
            final Long networkInstanceID = adapterDAO.findNetworkInstanceID(localInstanceID1);

            assertNotNull(networkInstanceID);

            assertEquals(Long.valueOf(instanceID1), networkInstanceID);
        }

        {
            final Long networkInstanceID = adapterDAO.findNetworkInstanceID(localInstanceID2);

            assertNotNull(networkInstanceID);

            assertEquals(Long.valueOf(instanceID2), networkInstanceID);
        }

        {
            //Look up bogus master and instance IDs
            final Long networkInstanceID = adapterDAO.findNetworkInstanceID("3847398Lasdkllasdk");

            assertNull(networkInstanceID);
        }
    }

    public void testFindNetworkInstanceIDNoMasterID() throws Exception {
        sanityCheckTestData();

        {
            final Long networkInstanceID = adapterDAO.findNetworkInstanceID(localInstanceID1);

            assertNotNull(networkInstanceID);

            assertEquals(Long.valueOf(instanceID1), networkInstanceID);
        }

        {
            final Long networkInstanceID = adapterDAO.findNetworkInstanceID(localInstanceID2);

            assertNotNull(networkInstanceID);

            assertEquals(Long.valueOf(instanceID2), networkInstanceID);
        }

        {
            //Look up bogus master and instance IDs
            final Long networkInstanceID = adapterDAO.findNetworkInstanceID("3847398Lasdkllasdk");

            assertNull(networkInstanceID);
        }
    }

    public void testFindLocalResultID() throws Exception {
        sanityCheckTestData();

        {
            final String localResultID = adapterDAO.findLocalResultID(resultID1a);

            assertNotNull(localResultID);

            assertEquals(localResultID1a, localResultID);
        }

        {
            final String localResultID = adapterDAO.findLocalResultID(resultID1b);

            assertNotNull(localResultID);

            assertEquals(localResultID1b, localResultID);
        }

        {
            //Look up bogus master ID
            final String localResultID = adapterDAO.findLocalResultID(384783L);

            assertNull(localResultID);
        }
    }

    public void testFindNetworkResultID() throws Exception {
        sanityCheckTestData();

        {
            final Long networkResultID = adapterDAO.findNetworkResultID(localResultID1a);

            assertNotNull(networkResultID);

            assertEquals(Long.valueOf(resultID1a), networkResultID);
        }

        {
            final Long networkResultID = adapterDAO.findNetworkResultID(localResultID1b);

            assertNotNull(networkResultID);

            assertEquals(Long.valueOf(resultID1b), networkResultID);
        }

        {
            //Look up bogus master, instance, and result IDs
            final Long networkResultID = adapterDAO.findNetworkResultID("384783Lsakjfha");

            assertNull(networkResultID);
        }
    }

    public void testFindNetworkResultIDNoMasterID() throws Exception {
        sanityCheckTestData();

        {
            final Long networkResultID = adapterDAO.findNetworkResultID(localResultID1a);

            assertNotNull(networkResultID);

            assertEquals(Long.valueOf(resultID1a), networkResultID);
        }

        {
            final Long networkResultID = adapterDAO.findNetworkResultID(localResultID1b);

            assertNotNull(networkResultID);

            assertEquals(Long.valueOf(resultID1b), networkResultID);
        }

        {
            //Look up bogus master, instance, and result IDs
            final Long networkResultID = adapterDAO.findNetworkResultID("384783Lsakjfha");

            assertNull(networkResultID);
        }
    }

    private void sanityCheckTestData() throws DAOException {
        //Sanity checks
        assertEquals(3, adapterDAO.getAuditEntries(identity1).size());
        assertEquals(1, adapterDAO.getAuditEntries(identity2).size());
    }

    public void testHappyCase() throws Exception {
        final int resultId = 999;
        final RequestResponseData mapping = new RequestResponseData("domain", "username", 1, 2, resultId, "ERROR", 10, 1000, "spin", "<resultXml/>");
        adapterDAO.insertRequestResponseData(mapping);
        final RequestResponseData selected = adapterDAO.findRequestResponseDataByResultID(resultId);
        assertEquals(mapping, selected);
    }

    public void testFindNetworkMasterIDsForUser() throws Exception {
        //Should be 2 master IDs
        {
            final List<Long> masterIDs = getMasterIDsFrom(adapterDAO.findNetworkMasterDefinitions(domain, username));

            assertNotNull(masterIDs);

            assertEquals(asSet(masterID1, masterID2, masterID3), asSet(masterIDs));
        }

        //Should be 1 master ID
        {
            final List<Long> masterIDs = getMasterIDsFrom(adapterDAO.findNetworkMasterDefinitions(identity2.getDomain(), identity2.getUsername()));

            assertNotNull(masterIDs);

            assertEquals(asSet(masterID4), asSet(masterIDs));
        }

        //Bogus user should have no master IDs
        {
            final List<Long> masterIDs = getMasterIDsFrom(adapterDAO.findNetworkMasterDefinitions("alskjdlaksjfhlkasfhlkajf", "aslkfljkasjfklashflhaskjg"));

            assertNotNull(masterIDs);

            assertTrue("Master ID list should be empty, but was " + masterIDs, masterIDs.isEmpty());
        }
    }

    public void testFindMasterQueryDefinition() throws DAOException {
        sanityCheckTestData();

        {
            final MasterQueryDefinition queryDefinition = adapterDAO.findMasterQueryDefinition(masterID1);

            assertNotNull(queryDefinition);

            assertEquals(master1.getQueryDefinition(), queryDefinition.getRequestXml());
        }

        {
            final MasterQueryDefinition queryDefinition = adapterDAO.findMasterQueryDefinition(masterID2);

            assertNotNull(queryDefinition);

            assertEquals(master2.getQueryDefinition(), queryDefinition.getRequestXml());
        }
    }

    static final List<Long> getMasterIDsFrom(final List<QueryMasterType> masterDefinitions) {
        if(masterDefinitions == null) {
            return null;
        }

        final List<Long> result = Util.makeArrayList(masterDefinitions.size());

        for(final QueryMasterType queryMaster : masterDefinitions) {
            result.add(Long.valueOf(queryMaster.getQueryMasterId()));
        }

        return result;
    }

    public void testFindObfuscationAmount() throws DAOException {
        sanityCheckTestData();

        {
            Integer actual = adapterDAO.findObfuscationAmount(String.valueOf(resultID1a));
            assertEquals(result1aObfuscationAmount, actual);
        }
        {
            Integer actual = adapterDAO.findObfuscationAmount(String.valueOf(resultID1b));
            assertEquals(result1bObfuscationAmount, actual);
        }
    }

    public void testUpdateObfuscationAmount() throws DAOException {
        sanityCheckTestData();

        Integer actual = adapterDAO.findObfuscationAmount(String.valueOf(resultID1a));
        assertEquals(result1aObfuscationAmount, actual);

        int newAmount = 10;
        assertTrue(result1aObfuscationAmount != newAmount);
        adapterDAO.updateObfuscationAmount(String.valueOf(resultID1a), newAmount);

        actual = adapterDAO.findObfuscationAmount(String.valueOf(resultID1a));
        assertEquals(newAmount, (int) actual);


    }


}
