package net.shrine.adapter.dao;

import net.shrine.protocol.QueryMaster;
import org.spin.tools.Util;
import org.spin.tools.crypto.signature.Identity;

import java.util.List;

import static org.spin.tools.Util.asSet;

/**
 * @author Justin Quan
 * @version %I% Date: Apr 12, 2010
 */
public abstract class AdapterDAOTest extends AbstractRequestResponseDataTest {

    public void testInsertAndFindUserThreshold() throws Exception {
        final Identity testId = new Identity("testDomain", "testUsername");
        final Integer testThreshold = 99;

        adapterDAO.insertUserThreshold(testId, testThreshold);

        assertEquals(testThreshold, adapterDAO.findUserThreshold(testId));
    }

    // NB: isUserLockedOut is exercised by PrivilegedUserTest

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
            // Look up bogus master ID
            final String localMasterID = adapterDAO.findLocalMasterID(239582L);

            assertNull("Should have been null, but was" + localMasterID, localMasterID);
        }
    }

    public void testFindNetworkMasterID() throws Exception {
        sanityCheckTestData();

        {
            final Long networkMasterID = (Long) adapterDAO.findNetworkMasterID(localMasterID1).get();

            assertNotNull(networkMasterID);

            assertEquals(Long.valueOf(masterID1), networkMasterID);
        }

        {
            final Long networkMasterID = (Long) adapterDAO.findNetworkMasterID(localMasterID2).get();

            assertNotNull(networkMasterID);

            assertEquals(Long.valueOf(masterID2), networkMasterID);
        }

        {
            // Look up bogus master ID
            assertTrue(adapterDAO.findNetworkMasterID("239582asklfjlasfj").isEmpty());
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
            // Look up bogus master and instance IDs
            final String localInstanceID = adapterDAO.findLocalInstanceID(3847398L);

            assertNull(localInstanceID);
        }
    }

    public void testFindNetworkInstanceID() throws Exception {
        sanityCheckTestData();

        {
            final Long networkInstanceID = (Long) adapterDAO.findNetworkInstanceID(localInstanceID1).get();

            assertNotNull(networkInstanceID);

            assertEquals(Long.valueOf(instanceID1), networkInstanceID);
        }

        {
            final Long networkInstanceID = (Long) adapterDAO.findNetworkInstanceID(localInstanceID2).get();

            assertNotNull(networkInstanceID);

            assertEquals(Long.valueOf(instanceID2), networkInstanceID);
        }

        {
            // Look up bogus master and instance IDs
            assertTrue(adapterDAO.findNetworkInstanceID("3847398Lasdkllasdk").isEmpty());
        }
    }

    public void testFindNetworkInstanceIDNoMasterID() throws Exception {
        sanityCheckTestData();

        {
            final Long networkInstanceID = (Long) adapterDAO.findNetworkInstanceID(localInstanceID1).get();

            assertNotNull(networkInstanceID);

            assertEquals(Long.valueOf(instanceID1), networkInstanceID);
        }

        {
            final Long networkInstanceID = (Long) adapterDAO.findNetworkInstanceID(localInstanceID2).get();

            assertNotNull(networkInstanceID);

            assertEquals(Long.valueOf(instanceID2), networkInstanceID);
        }

        {
            // Look up bogus master and instance IDs
            assertTrue(adapterDAO.findNetworkInstanceID("3847398Lasdkllasdk").isEmpty());
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
            // Look up bogus master ID
            final String localResultID = adapterDAO.findLocalResultID(384783L);

            assertNull(localResultID);
        }
    }

    public void testFindNetworkResultID() throws Exception {
        sanityCheckTestData();

        {
            final Long networkResultID = (Long) adapterDAO.findNetworkResultID(localResultID1a).get();

            assertNotNull(networkResultID);

            assertEquals(Long.valueOf(resultID1a), networkResultID);
        }

        {
            final Long networkResultID = (Long) adapterDAO.findNetworkResultID(localResultID1b).get();

            assertNotNull(networkResultID);

            assertEquals(Long.valueOf(resultID1b), networkResultID);
        }

        {
            // Look up bogus master, instance, and result IDs
            assertTrue(adapterDAO.findNetworkResultID("384783Lsakjfha").isEmpty());
        }
    }

    public void testFindNetworkResultIDNoMasterID() throws Exception {
        sanityCheckTestData();

        {
            final Long networkResultID = (Long) adapterDAO.findNetworkResultID(localResultID1a).get();

            assertNotNull(networkResultID);

            assertEquals(Long.valueOf(resultID1a), networkResultID);
        }

        {
            final Long networkResultID = (Long) adapterDAO.findNetworkResultID(localResultID1b).get();

            assertNotNull(networkResultID);

            assertEquals(Long.valueOf(resultID1b), networkResultID);
        }

        {
            // Look up bogus master, instance, and result IDs
            assertTrue(adapterDAO.findNetworkResultID("384783Lsakjfha").isEmpty());
        }
    }

    private void sanityCheckTestData() throws DAOException {
        // Sanity checks
        assertEquals(3, adapterDAO.getAuditEntries(identity1).size());
        assertEquals(1, adapterDAO.getAuditEntries(identity2).size());
    }

    public void testHappyCase() throws Exception {
        final int resultId = 999;
        final RequestResponseData mapping = new RequestResponseData("domain", "username", 1, 2, resultId, "ERROR", 10, 1000, "spin", "<resultXml/>");
        adapterDAO.insertRequestResponseData(mapping);
        final RequestResponseData selected = adapterDAO.findRequestResponseDataByResultID(resultId).get();
        assertEquals(mapping, selected);
    }

    public void testFindNetworkMasterIDsForUser() throws Exception {
        // Should be 2 master IDs
        {
            final List<Long> masterIDs = getMasterIDsFrom(adapterDAO.findNetworkMasterDefinitions(domain, username));

            assertNotNull(masterIDs);

            assertEquals(asSet(masterID1, masterID2, masterID3), asSet(masterIDs));
        }

        // Should be 1 master ID
        {
            final List<Long> masterIDs = getMasterIDsFrom(adapterDAO.findNetworkMasterDefinitions(identity2.getDomain(), identity2.getUsername()));

            assertNotNull(masterIDs);

            assertEquals(asSet(masterID4), asSet(masterIDs));
        }

        // Bogus user should have no master IDs
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

    static final List<Long> getMasterIDsFrom(final scala.collection.Seq<QueryMaster> masterDefinitions) {
        if (masterDefinitions == null) {
            return null;
        }

        final List<Long> result = Util.makeArrayList(masterDefinitions.size());

        for (final QueryMaster queryMaster : scala.collection.JavaConversions.asJavaIterable(masterDefinitions)) {
            result.add(Long.valueOf(queryMaster.queryMasterId()));
        }

        return result;
    }

    public void testFindObfuscationAmount() throws DAOException {
        sanityCheckTestData();

        {
            Integer actual = (Integer) adapterDAO.findObfuscationAmount(String.valueOf(resultID1a)).get();
            assertEquals(result1aObfuscationAmount, actual);
        }
        {
            Integer actual = (Integer) adapterDAO.findObfuscationAmount(String.valueOf(resultID1b)).get();
            assertEquals(result1bObfuscationAmount, actual);
        }
    }

    public void testUpdateObfuscationAmount() throws DAOException {
        sanityCheckTestData();

        Integer actual = (Integer) adapterDAO.findObfuscationAmount(String.valueOf(resultID1a)).get();
        assertEquals(result1aObfuscationAmount, actual);

        int newAmount = 10;
        assertTrue(result1aObfuscationAmount != newAmount);
        adapterDAO.updateObfuscationAmount(String.valueOf(resultID1a), newAmount);

        actual = (Integer) adapterDAO.findObfuscationAmount(String.valueOf(resultID1a)).get();
        assertEquals(newAmount, (int) actual);
    }
}
