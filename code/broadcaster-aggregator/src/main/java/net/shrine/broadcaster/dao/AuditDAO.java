package net.shrine.broadcaster.dao;


import net.shrine.broadcaster.dao.hibernate.AuditEntry;

import java.util.Date;
import java.util.List;

public interface AuditDAO {

    public void addAuditEntry(AuditEntry auditEntryEntry);

    List<AuditEntry> findRecentEntries(int limit);
}
