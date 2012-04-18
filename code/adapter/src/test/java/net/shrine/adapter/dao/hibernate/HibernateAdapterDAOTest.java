package net.shrine.adapter.dao.hibernate;

import net.shrine.adapter.dao.AdapterDAOTest;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @author ???
 * @date ???
 */
public final class HibernateAdapterDAOTest extends AdapterDAOTest
{
    @Resource
    DataSource dataSource;

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        insertRequestResponseDatas();

        insertMasterQuerys();

        insertInstanceIDPairs();

        insertResultIDPairs();

        mapUsersToMasterIDs();
    }

    @Override
    protected String getConfigPath()
    {
        return "/testApplicationContext.xml";
    }
}
