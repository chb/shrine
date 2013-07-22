package net.shrine.adapter.query;

import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import junit.framework.TestCase;
import net.shrine.adapter.dao.UserAndMaster;
import org.spin.tools.NetworkTime;

import java.util.Date;

/**
 * @author clint
 *         <p/>
 *         Sep 8, 2010
 *         <p/>
 *         Center for Biomedical Informatics (CBMI)
 * @link https://cbmi.med.harvard.edu/
 */
public final class UserAndMasterTest extends TestCase
{
    public void testToQueryMasterType() throws Exception
    {
        final String domain = "somewhere";
        final String user = "some guy";
        final Long networkMasterID = 123L;
        final String masterName = "some query";
        final Date now = new Date();

        final UserAndMaster userAndMaster = new UserAndMaster(domain, user, networkMasterID, masterName, now);

        final QueryMasterType queryMasterType = userAndMaster.toQueryMasterType();

        assertNotNull(queryMasterType);

        assertEquals(domain, queryMasterType.getGroupId());
        assertEquals(user, queryMasterType.getUserId());
        assertEquals(String.valueOf(networkMasterID), queryMasterType.getQueryMasterId());
        assertEquals(masterName, queryMasterType.getName());
        assertEquals(NetworkTime.makeXMLGregorianCalendar(now), queryMasterType.getCreateDate());
    }
}
