package net.shrine.adapter.query;

import net.shrine.adapter.dao.AdapterDAO;
import net.shrine.adapter.dao.DAOException;
import net.shrine.adapter.dao.RequestResponseData;
import org.junit.Test;
import org.spin.tools.crypto.signature.Identity;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import javax.annotation.Resource;

/**
 * @author Bill Simons
 * @date Nov 23, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */

public class PrivilegedUserTest extends AbstractTransactionalDataSourceSpringContextTests
{
    @Resource
    protected AdapterDAO adapterDAO;

    protected String testDomain = "testDomain";
    protected String testUsername = "testUsername";
    protected Integer testThreshold = 3;
    protected Integer defaultThreshold = 10;

    @Override
    protected String getConfigPath()
    {
        return "/testApplicationContext.xml";
    }

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        adapterDAO.insertUserThreshold(testUsername, testThreshold);
    }

    @Test
    public void testGetUserThreshold() throws DAOException
    {
        int threshold = adapterDAO.findUserThreshold(testUsername);
        assertEquals((int) testThreshold, threshold);
    }

    @Test
    public void testIsUserWithNoThresholdEntryLockedOut() throws DAOException
    {
        String username = "noEntry";
        assertFalse(adapterDAO.isUserLockedOut(new Identity(testDomain, username), defaultThreshold));
        lockoutUser(username, 42);
        assertFalse(adapterDAO.isUserLockedOut(new Identity(testDomain, username), defaultThreshold));
    }

    @Test
    public void testIsUserLockedOut() throws DAOException
    {
        assertFalse(adapterDAO.isUserLockedOut(new Identity(testDomain, testUsername), defaultThreshold));
        insertRequestResponse(0, testUsername, 42);
        assertFalse(adapterDAO.isUserLockedOut(new Identity(testDomain, testUsername), defaultThreshold));
        lockoutUser(testUsername, 42);
        assertTrue(adapterDAO.isUserLockedOut(new Identity(testDomain, testUsername), defaultThreshold));
    }

    @Test
    public void testIsUserLockedOutWithResultSetSizeOfZero() throws DAOException
    {
        assertFalse(adapterDAO.isUserLockedOut(new Identity(testDomain, testUsername), defaultThreshold));
        insertRequestResponse(0, testUsername, 0);
        assertFalse(adapterDAO.isUserLockedOut(new Identity(testDomain, testUsername), defaultThreshold));
        lockoutUser(testUsername, 0);

        //user should not be locked out
        assertFalse(adapterDAO.isUserLockedOut(new Identity(testDomain, testUsername), defaultThreshold));
    }

    private void lockoutUser(final String lockedOutUsername, final int resultSetSize) throws DAOException
    {
        for(int i = 1; i < (testThreshold + 1); i++)
        {
            insertRequestResponse(i, lockedOutUsername, resultSetSize);
        }
    }

    private void insertRequestResponse(int queryId, final String lockedOutUsername, final int resultSetSize)
            throws DAOException
    {
        RequestResponseData data = new RequestResponseData(testDomain, lockedOutUsername, queryId, queryId, queryId, "OK", resultSetSize, 12, String.valueOf(queryId), "<result_xml/>");
        adapterDAO.insertRequestResponseData(data);
    }
}
