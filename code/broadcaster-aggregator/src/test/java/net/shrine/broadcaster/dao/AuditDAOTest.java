package net.shrine.broadcaster.dao;


import net.shrine.broadcaster.dao.hibernate.AuditEntry;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import java.util.Date;
import java.util.List;

public class AuditDAOTest extends AbstractTransactionalDataSourceSpringContextTests {

    @Autowired
    private AuditDAO auditDao;

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"classpath:testApplicationContext.xml"};
    }

    @Test
    public void testGetRecentEntries() throws Exception {
        int limit = 10;
        List<AuditEntry> entries = auditDao.findRecentEntries(limit);
        assertNotNull(entries);
        assertFalse(entries.isEmpty());
        assertEquals(limit, entries.size());
        assertDateInDescendingOrder(limit, entries);
    }

    private void assertDateInDescendingOrder(int limit, List<AuditEntry> entries) {
        for(int i = 0; i < limit - 1; i++) {
            assertTrue(entries.get(i).getTime().getTime() > entries.get(i+1).getTime().getTime());
        }
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        for(int i = 0; i < 20; i++) {
            auditDao.addAuditEntry(newAuditEntry(new Date(1000 * i)));
        }
    }

    private AuditEntry newAuditEntry(final Date date) {
        AuditEntry entry = new AuditEntry();
        entry.setDomain("domain");
        entry.setQueryText("query");
        entry.setUsername("username");
        entry.setProject("project");
        entry.setQueryTopic("topic");
        entry.setTime(date);
        return entry;
    }
}
