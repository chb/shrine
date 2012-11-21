package net.shrine.adapter.dao.hibernate.entity;

import net.shrine.adapter.dao.LegacyAdapterDAO;
import net.shrine.adapter.dao.DAOException;
import net.shrine.adapter.dao.RequestResponseData;
import org.junit.Test;
import org.spin.tools.Util;
import org.spin.tools.crypto.signature.Identity;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import javax.annotation.Resource;

import java.util.Calendar;
import java.util.Date;

import static java.util.Arrays.asList;

/**
 * @author Bill Simons
 * @date Nov 23, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */

public class PrivilegedUserTest extends AbstractTransactionalDataSourceSpringContextTests {
    @Resource
    protected LegacyAdapterDAO adapterDAO;

    protected final String testDomain = "testDomain";
    protected final String testUsername = "testUsername";
    protected final Identity testId = new Identity(testDomain, testUsername);
    protected final Integer testThreshold = 3;
    protected final Integer defaultThreshold = 10;

    @Override
    protected String getConfigPath() {
        return "/testApplicationContext.xml";
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        adapterDAO.insertUserThreshold(testId, testThreshold);
    }

    @Test
    public void testGetUserThreshold() throws DAOException {
        final int threshold = adapterDAO.findUserThreshold(testId);

        assertEquals((int) testThreshold, threshold);
    }

    @Test
    public void testIsUserWithNoThresholdEntryLockedOut() throws DAOException {
        final String username = "noEntry";
        final String domain = "noEntryDomain";

        final Identity noThresholdId1 = new Identity(testDomain, username);
        final Identity noThresholdId2 = new Identity(domain, testUsername);

        for (final Identity noThreshold : asList(noThresholdId1, noThresholdId2)) {
            assertFalse(adapterDAO.isUserLockedOut(noThreshold, defaultThreshold));

            lockoutUser(noThreshold, 42);

            assertFalse(adapterDAO.isUserLockedOut(noThreshold, defaultThreshold));
        }
    }

    @Test
    public void testIsUserLockedOut() throws DAOException {
        assertFalse(adapterDAO.isUserLockedOut(testId, defaultThreshold));

        insertRequestResponse(0, testId, 42);

        assertFalse(adapterDAO.isUserLockedOut(testId, defaultThreshold));

        lockoutUser(testId, 42);

        assertTrue(adapterDAO.isUserLockedOut(testId, defaultThreshold));

        // Make sure username + domain is how users are identified
        assertFalse(adapterDAO.isUserLockedOut(new Identity(testDomain, "some-other-username"), defaultThreshold));
        assertFalse(adapterDAO.isUserLockedOut(new Identity("some-other-domain", testUsername), defaultThreshold));
    }

    @Test
    public void testIsUserLockedOutWithResultSetSizeOfZero() throws DAOException {
        assertFalse(adapterDAO.isUserLockedOut(testId, defaultThreshold));

        insertRequestResponse(0, testId, 0);

        assertFalse(adapterDAO.isUserLockedOut(testId, defaultThreshold));

        lockoutUser(testId, 0);

        // user should not be locked out
        assertFalse(adapterDAO.isUserLockedOut(testId, defaultThreshold));
    }

    @Test
    public void testLockoutOverride() throws DAOException {
        assertFalse(adapterDAO.isUserLockedOut(testId, defaultThreshold));
        lockoutUser(testId, 42);
        assertTrue(adapterDAO.isUserLockedOut(testId, defaultThreshold));

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        adapterDAO.overrideLockout(testId, tomorrow.getTime());

        assertFalse(adapterDAO.isUserLockedOut(testId, defaultThreshold));

        Calendar thirtyOneDaysAgo = Calendar.getInstance();
        thirtyOneDaysAgo.add(Calendar.DAY_OF_MONTH, -31);
        adapterDAO.overrideLockout(testId, thirtyOneDaysAgo.getTime());
    }

    private void lockoutUser(final Identity lockedOutId, final int resultSetSize) throws DAOException {
        for (int i : Util.range(1, testThreshold + 2)) {
            insertRequestResponse(i, lockedOutId, resultSetSize);
        }
    }

    private void insertRequestResponse(int queryId, final Identity lockedOutId, final int resultSetSize) throws DAOException {
        RequestResponseData data = new RequestResponseData(lockedOutId.getDomain(), lockedOutId.getUsername(), queryId, queryId, queryId, "OK", resultSetSize, 12, String.valueOf(queryId), "<result_xml/>");

        adapterDAO.insertRequestResponseData(data);
    }
}
