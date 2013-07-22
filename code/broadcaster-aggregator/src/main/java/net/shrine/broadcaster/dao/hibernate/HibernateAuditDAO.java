package net.shrine.broadcaster.dao.hibernate;


import net.shrine.broadcaster.dao.AuditDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * DAO that writes audit entries
 */
@Repository
public class HibernateAuditDAO implements AuditDAO {

    @Resource
    SessionFactory sessionFactory;

    @Override
    public void addAuditEntry(AuditEntry auditEntry) {
        Session s = sessionFactory.getCurrentSession();
        s.saveOrUpdate(auditEntry);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AuditEntry> findRecentEntries(int limit) {
        Session s = sessionFactory.getCurrentSession();
        return s.createCriteria(AuditEntry.class)
                .addOrder(Order.desc("time"))
                .setMaxResults(limit)
                .list();
    }
}


