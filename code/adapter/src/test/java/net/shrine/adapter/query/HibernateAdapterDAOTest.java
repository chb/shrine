package net.shrine.adapter.query;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 *
 *
 */


public class HibernateAdapterDAOTest extends AdapterDAOTest
{

    @Resource
    DataSource dataSource;


    @Override
    protected void onSetUpInTransaction() throws Exception
    {


//        DDLUtil.createSchema(DDLUtil.DatabaseType.HSQL, dataSource);

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
