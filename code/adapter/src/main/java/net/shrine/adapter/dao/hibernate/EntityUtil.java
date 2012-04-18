package net.shrine.adapter.dao.hibernate;


import net.shrine.adapter.dao.MasterTuple;
import net.shrine.adapter.dao.RequestResponseData;
import net.shrine.adapter.dao.hibernate.entity.MasterQueryEntity;
import net.shrine.adapter.dao.hibernate.entity.RequestResponseDataEntity;
import org.apache.log4j.Logger;
import org.spin.tools.Util;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @Author David Ortiz
 * <p/>
 * Just a class to convert between Hibernate Entity types and our standard types
 */
public class EntityUtil {
    static Logger log = Logger.getLogger(EntityUtil.class);


    public static RequestResponseData convertFromEntity(RequestResponseDataEntity entity) {
        Util.guardNotNull(entity);

        RequestResponseData data = new RequestResponseData();
        data.setBroadcastQueryInstanceId(entity.getBroadcastQueryInstanceId());
        data.setBroadcastQueryMasterId(entity.getBroadcastQueryMasterId());
        data.setBroadcastResultInstanceId(entity.getBroadcastResultInstanceId());
        data.setUsername(entity.getUsername());
        data.setDomainName(entity.getDomainName());
        data.setResultSetSize(entity.getResultSetSize());
        data.setResultStatus(entity.getResultStatus());
        data.setResultXml(entity.getResultXml());
        data.setSpinQueryId(entity.getSpinQueryId());
        data.setTimeElapsedMillis(entity.getTimeElapsed());


        return data;

    }


    public static RequestResponseDataEntity convertToEntity(RequestResponseData requestResponseData) {
        Util.guardNotNull(requestResponseData);

        RequestResponseDataEntity entity = new RequestResponseDataEntity();
        entity.setUsername(requestResponseData.getUsername());
        entity.setBroadcastQueryInstanceId(requestResponseData.getBroadcastQueryInstanceId());
        entity.setBroadcastQueryMasterId(requestResponseData.getBroadcastQueryMasterId());
        entity.setBroadcastResultInstanceId(requestResponseData.getBroadcastResultInstanceId());
        entity.setDomainName(requestResponseData.getDomainName());
        entity.setResultSetSize(requestResponseData.getResultSetSize());
        entity.setResultStatus(requestResponseData.getResultStatus());
        entity.setResultXml(requestResponseData.getResultXml());
        entity.setSpinQueryId(requestResponseData.getSpinQueryId());

        Date d = new Date();
        entity.setQueryDatetime(d);

        //awful check the schema
        entity.setTimeElapsed((int) requestResponseData.getTimeElapsedMillis());
        return entity;
    }

    public static MasterQueryEntity convertToEntity(MasterTuple tuple) {
        Util.guardNotNull(tuple);
        MasterQueryEntity entity = new MasterQueryEntity();
        entity.setBroadcastQueryMasterId(tuple.getIdPair().getNetworkID());
        entity.setLocalQueryMasterId(tuple.getIdPair().getLocalID());
        entity.setQueryDefinition(tuple.getQueryDefinition());
        return entity;
    }

}
