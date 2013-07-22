package net.shrine.adapter.dao.hibernate;


import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import net.shrine.adapter.dao.AdapterDAO;
import net.shrine.adapter.dao.DAOException;
import net.shrine.adapter.dao.IDPair;
import net.shrine.adapter.dao.MasterQueryDefinition;
import net.shrine.adapter.dao.MasterTuple;
import net.shrine.adapter.dao.RequestResponseData;
import net.shrine.adapter.dao.ResultTuple;
import net.shrine.adapter.dao.UserAndMaster;
import net.shrine.adapter.dao.hibernate.entity.InstanceIdsEntity;
import net.shrine.adapter.dao.hibernate.entity.MasterQueryEntity;
import net.shrine.adapter.dao.hibernate.entity.PrivilegedUserEntity;
import net.shrine.adapter.dao.hibernate.entity.RequestResponseDataEntity;
import net.shrine.adapter.dao.hibernate.entity.ResultIdsEntity;
import net.shrine.adapter.dao.hibernate.entity.UsersToMasterQueryEntity;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.spin.tools.NetworkTime;
import org.spin.tools.crypto.signature.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.spin.tools.Util.makeArrayList;


@Repository
public class HibernateAdapterDAO implements AdapterDAO {

    private static final Logger log = Logger.getLogger(HibernateAdapterDAO.class);

    private final boolean DEBUG = log.isDebugEnabled();

    @Autowired
    SessionFactory sessionFactory;


    @Override
    public RequestResponseData findRequestResponseDataByResultID(long resultID) throws DAOException {
        Session s = null;
        try {

            s = sessionFactory.getCurrentSession();
            Criteria criteria = s.createCriteria(RequestResponseDataEntity.class)
                    .add(Restrictions.eq("broadcastResultInstanceId", resultID));

            List<RequestResponseDataEntity> resultList = criteria.list();

            if(resultList.size() > 1) {
                throw new DAOException("Database is in inconsistent state");
            }
            else if(resultList.size() == 0) {
                return null;
            }
            else {
                return EntityUtil.convertFromEntity(resultList.get(0));
            }
        } catch(HibernateException e) {
            throw new DAOException("Error querying database");

        }
    }

    @Override
    public void insertRequestResponseData(RequestResponseData requestResponseData) throws DAOException {
        RequestResponseDataEntity entity = EntityUtil.convertToEntity(requestResponseData);
        create(entity);
    }

    @Override
    public void insertMasterIDPair(IDPair idPair) throws DAOException {
        MasterQueryEntity entity = new MasterQueryEntity();
        entity.setBroadcastQueryMasterId(idPair.getNetworkID());
        entity.setLocalQueryMasterId(idPair.getLocalID());
        create(entity);

    }

    @Override
    public void insertMaster(MasterTuple tuple) throws DAOException {
        MasterQueryEntity entity = EntityUtil.convertToEntity(tuple);
        create(entity);


    }

    @Override
    public void insertInstanceIDPair(IDPair idPair) throws DAOException {
        InstanceIdsEntity entity = new InstanceIdsEntity();
        entity.setBroadcastQueryInstanceId(idPair.getNetworkID());
        entity.setLocalQueryInstanceId(idPair.getLocalID());
        create(entity);
    }

    @Override
    public void insertResultTuple(ResultTuple tuple) throws DAOException {
        ResultIdsEntity entity = new ResultIdsEntity();
        if(tuple.getObfuscationAmount() != null) {
            entity.setObfuscationAmount(tuple.getObfuscationAmount());
        }
        entity.setBroadcastResultInstanceId(tuple.getIdPair().getNetworkID());
        entity.setLocalResultInstanceId(tuple.getLocalID());
        create(entity);


    }

    @Override
    public void insertUserAndMasterIDMapping(UserAndMaster mapping) throws DAOException {

        UsersToMasterQueryEntity entity = new UsersToMasterQueryEntity();
        entity.setBroadcastQueryMasterId(mapping.getNetworkMasterID());
        entity.setDomainName(mapping.getDomainName());
        entity.setMasterCreateDate(new Timestamp(mapping.getMasterCreateDate().getTime()));
        entity.setMasterName(mapping.getMasterName());
        entity.setUsername(mapping.getUserName());

        create(entity);
    }

    @Override
    public List<RequestResponseData> getAuditEntries(Identity id) throws DAOException {
        List<RequestResponseData> returnList = makeArrayList();

        for(RequestResponseDataEntity entity : getAuditEntryEntities(id, null)) {
            returnList.add(EntityUtil.convertFromEntity(entity));
        }
        return returnList;

    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UserAndMaster> findRecentQueries(int limit) throws DAOException {
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            List<UsersToMasterQueryEntity> recentQueries = s.createCriteria(UsersToMasterQueryEntity.class)
                    .addOrder(Order.desc("masterCreateDate"))
                    .setMaxResults(limit)
                    .list();
            List<UserAndMaster> returnList = makeArrayList();

            for(UsersToMasterQueryEntity query : recentQueries) {
                returnList.add(new UserAndMaster(query.getDomainName(), query.getUsername(), query.getBroadcastQueryMasterId(), query.getMasterName(), new Date(query.getMasterCreateDate().getTime())));
            }

            return returnList;

        } catch(HibernateException e) {
            throw new DAOException("Error finding recent queries", e);
        }
    }

    private List<RequestResponseDataEntity> getAuditEntryEntities(final Identity id, final Session session) throws DAOException {
        Session s = null;
        try {
            if(session == null) {
                s = sessionFactory.getCurrentSession();
            }
            else {
                s = session;
            }


            Criteria criteria = s.createCriteria(RequestResponseDataEntity.class);
            criteria.add(Restrictions.eq("username", id.getUsername()));
            criteria.add(Restrictions.eq("domainName", id.getDomain()));

            List<RequestResponseDataEntity> responseList = criteria.list();
            return responseList;


        } catch(HibernateException e) {
            throw new DAOException("Error getting audit entries");
        }
    }


    @Override
    public boolean isUserLockedOut(Identity id, Integer defaultThreshold) {
        Session s = null;

        try {
            s = sessionFactory.getCurrentSession();
            Query q = s.createQuery("select user.threshold " +
                    "from PrivilegedUserEntity user " +
                    "where user.username = :username");

            q.setString("username", id.getUsername());

            Integer threshold = null;
            Object result = q.uniqueResult();

            if(result != null) {
                threshold = (Integer) result;

            }

            threshold = (threshold != null) ? threshold : defaultThreshold;

            Calendar today = Calendar.getInstance();
            today.add(Calendar.DAY_OF_MONTH, -30);
            Date thirtyDaysInThePast = today.getTime();


            Query q1 = s.createQuery("select count(e.resultSetSize) as setSize from RequestResponseDataEntity e" +
                    " where e.username = :username" +
                    " AND e.resultSetSize != 0" +
                    " AND e.queryDatetime > :thirtyDaysAgo" +
                    " group by e.resultSetSize");

            q1.setString("username", id.getUsername());
            q1.setDate("thirtyDaysAgo", thirtyDaysInThePast);


            List<Long> l = (List<Long>) q1.list();

            Collections.sort(l);

            Integer repeatedResultCount = null;

            int index = l.size() - 1;
            if(index >= 0) {
                repeatedResultCount = l.get(index).intValue();
            }


            repeatedResultCount = (repeatedResultCount != null) ? repeatedResultCount : 0;
            return repeatedResultCount > threshold;

        } catch(HibernateException e) {
            e.printStackTrace();

            return true;

        }

    }

    @Override
    public MasterQueryDefinition findMasterQueryDefinition(Long broadcastMasterID) throws DAOException {
        Session s = null;
        try {

            s = sessionFactory.getCurrentSession();
            Query q = s.createQuery("from MasterQueryEntity master where " +
                    "master.broadcastQueryMasterId = :broadcastMasterId  ");

            q.setLong("broadcastMasterId", broadcastMasterID);
            MasterQueryEntity result = (MasterQueryEntity) q.uniqueResult();


            if(result != null) {
                q = s.createQuery("from UsersToMasterQueryEntity entity " +
                        "where entity.broadcastQueryMasterId = :masterQueryId");
                q.setLong("masterQueryId", result.getBroadcastQueryMasterId());


                UsersToMasterQueryEntity usersToMaster = (UsersToMasterQueryEntity) q.uniqueResult();

                if(usersToMaster != null) {
                    MasterQueryDefinition returnValue = new MasterQueryDefinition();
                    returnValue.setUserId(usersToMaster.getUsername());
                    returnValue.setGroupId(usersToMaster.getDomainName());
                    returnValue.setName(usersToMaster.getMasterName());
                    returnValue.setCreateDate(usersToMaster.getMasterCreateDate());
                    returnValue.setQueryMasterId(result.getLocalQueryMasterId());
                    returnValue.setRequestXml(result.getQueryDefinition());
                    return returnValue;
                }
                throw new DAOException("Inconsistent database");
            }

        } catch(HibernateException e) {
            throw new DAOException("Unexpected Error");

        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String findLocalMasterID(Long broadcastMasterID) throws DAOException {

        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            Query q = s.createQuery("select entity.localQueryMasterId from MasterQueryEntity entity " +
                    "where entity.broadcastQueryMasterId = :masterId");

            q.setLong("masterId", broadcastMasterID);
            return (String) q.uniqueResult();

        } catch(HibernateException e) {
            throw new DAOException("Error querying");
        }
    }

    @Override
    public String findLocalInstanceID(Long broadcastInstanceID) throws DAOException {
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            Query q = s.createQuery("select entity.localQueryInstanceId from InstanceIdsEntity entity " +
                    "where entity.broadcastQueryInstanceId = :broadcastInstanceID");

            q.setLong("broadcastInstanceID", broadcastInstanceID);

            return (String) q.uniqueResult();
        } catch(HibernateException e) {
            throw new DAOException("Error querying");
        }
    }

    @Override
    public String findLocalResultID(Long broadcastResultID) throws DAOException {

        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();

            Query q = s.createQuery("select entity.localResultInstanceId from ResultIdsEntity entity " +
                    "where entity.broadcastResultInstanceId = :broadcastResultId");

            q.setLong("broadcastResultId", broadcastResultID);
            return (String) q.uniqueResult();

        } catch(HibernateException e) {
            throw new DAOException("Error querying");
        }
    }

    @Override
    public Long findNetworkMasterID(String localMasterID) throws DAOException {
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();

            Query q = s.createQuery("select entity.broadcastQueryMasterId from MasterQueryEntity entity " +
                    "where entity.localQueryMasterId = :localMasterId");
            q.setString("localMasterId", localMasterID);

            return (Long) q.uniqueResult();

        } catch(HibernateException e) {
            throw new DAOException("Error querying");
        }
    }

    @Override
    public Long findNetworkInstanceID(String localInstanceID) throws DAOException {
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            Query q = s.createQuery("select entity.broadcastQueryInstanceId from InstanceIdsEntity entity " +
                    "where entity.localQueryInstanceId = :localInstanceId ");

            q.setString("localInstanceId", localInstanceID);
            return (Long) q.uniqueResult();


        } catch(HibernateException e) {
            throw new DAOException("Error querying");
        }
    }

    @Override
    public Long findNetworkResultID(String localResultID) throws DAOException {
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            Query q = s.createQuery("select entity.broadcastResultInstanceId from ResultIdsEntity entity " +
                    "where entity.localResultInstanceId = :localResultInstanceId");

            q.setString("localResultInstanceId", localResultID);

            return (Long) q.uniqueResult();

        } catch(HibernateException e) {
            throw new DAOException("Error querying");
        }
    }

    @Override
    public List<QueryMasterType> findNetworkMasterDefinitions(String domainName, String userName) throws DAOException {
        List<QueryMasterType> returnList = new ArrayList<QueryMasterType>();
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            Collection<Integer> masterIds;
            Query q = s.createQuery("select userToMaster from UsersToMasterQueryEntity " +
                    "userToMaster " +
                    "where userToMaster.username = :username " +
                    "and userToMaster.domainName = :domainName order by userToMaster.masterCreateDate desc");

            q.setString("username", userName);
            q.setString("domainName", domainName);


            for(UsersToMasterQueryEntity entity :
                    (List<UsersToMasterQueryEntity>) q.list()) {

                QueryMasterType tempMasterType = new QueryMasterType();
                tempMasterType.setUserId(entity.getUsername());
                tempMasterType.setCreateDate(NetworkTime
                        .makeXMLGregorianCalendar(entity.getMasterCreateDate()));
                tempMasterType.setName(entity.getMasterName());
                tempMasterType.setGroupId(entity.getDomainName());
                tempMasterType.setQueryMasterId(String.valueOf(entity.getBroadcastQueryMasterId()));

                returnList.add(tempMasterType);
            }


            return returnList;

        } catch(HibernateException e) {
            throw new DAOException("Error querying", e);
        }
    }

    @Override
    public Integer findObfuscationAmount(String networkResultId) throws DAOException {

        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            Query q = s.createQuery("select entity.obfuscationAmount from ResultIdsEntity entity " +
                    "where entity.broadcastResultInstanceId = :networkResultId");

            q.setString("networkResultId", networkResultId);

            return (Integer) q.uniqueResult();

        } catch(HibernateException e) {
            throw new DAOException("Error querying");
        }
    }

    @Override
    public void updateObfuscationAmount(String networkResultId, int obfuscationAmount) throws DAOException {
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            Query q = s.createQuery("from ResultIdsEntity entity " +
                    "where entity.broadcastResultInstanceId = :networkResultId");

            q.setString("networkResultId", networkResultId);

            ResultIdsEntity result = (ResultIdsEntity) q.uniqueResult();
            result.setObfuscationAmount(obfuscationAmount);
            s.saveOrUpdate(result);
        } catch(HibernateException e) {
            throw new DAOException("Error querying");
        }
    }

    @Override
    public void removeMasterDefinitions(Long networkMasterId) throws DAOException {
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            Query q = s.createQuery("delete from MasterQueryEntity master where master.broadcastQueryMasterId = :id");
            q.setLong("id", networkMasterId);
            q.executeUpdate();

        } catch(HibernateException e) {
            throw new DAOException("Error saving object to database");
        }
    }

    @Override
    public void removeUserToMasterMapping(Long networkMasterId) throws DAOException {
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            Query q = s.createQuery("delete from UsersToMasterQueryEntity master where master.broadcastQueryMasterId = :id");
            q.setLong("id", networkMasterId);
            q.executeUpdate();

        } catch(HibernateException e) {
            throw new DAOException("Error saving object to database");
        }
    }

    @Override
    public void updateUsersToMasterQueryName(Long networkMasterId, String queryName) throws DAOException {
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            Query q = s.createQuery("from UsersToMasterQueryEntity entity " +
                    "where entity.broadcastQueryMasterId= :id");

            q.setLong("id", networkMasterId);

            UsersToMasterQueryEntity result = (UsersToMasterQueryEntity) q.uniqueResult();
            result.setMasterName(queryName);
            s.saveOrUpdate(result);
        } catch(HibernateException e) {
            throw new DAOException("Error saving object to database");
        }
    }


    @Override
    public int findUserThreshold(String username) throws DAOException {
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            Query q = s.createQuery("select entity.threshold from PrivilegedUserEntity entity " +
                    "where entity.username = :username");

            q.setString("username", username);


            return (Integer) q.uniqueResult();

        } catch(HibernateException e) {
            throw new DAOException("Error querying");
        }
    }

    @Override
    public void insertUserThreshold(String username, Integer threshold) throws DAOException {
        Session s = null;
        try {

            s = sessionFactory.getCurrentSession();

            Query q = s.createQuery("from PrivilegedUserEntity p where p.username = :userName");
            q.setString("userName", username);

            PrivilegedUserEntity entity = (PrivilegedUserEntity) q.uniqueResult();
            if(entity == null) {
                entity = new PrivilegedUserEntity();
                entity.setThreshold(threshold);
                entity.setUsername(username);
                s.save(entity);
            }
            else {
                entity.setThreshold(threshold);
                s.saveOrUpdate(entity);
            }


        } catch(HibernateException e) {
            throw new DAOException("Error saving object to database");
        }
    }

    private void create(Object o) throws DAOException {
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            s.saveOrUpdate(o);

        } catch(HibernateException e) {
            throw new DAOException("Error saving object to database", e);
        }

    }
}
