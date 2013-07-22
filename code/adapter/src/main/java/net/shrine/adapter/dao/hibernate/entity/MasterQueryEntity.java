package net.shrine.adapter.dao.hibernate.entity;

import javax.persistence.*;


/**
 * Created by IntelliJ IDEA.
 * User: davidortiz
 * Date: 12/13/10
 * Time: 9:19 AM
 * To change this template use File | Settings | File Templates.
 */
@Table(name = "MASTER_QUERY")
@Entity
public class MasterQueryEntity {
    private long broadcastQueryMasterId;

    @Column(name = "BROADCAST_QUERY_MASTER_ID")
    @Id
    public long getBroadcastQueryMasterId() {
        return broadcastQueryMasterId;
    }

    public void setBroadcastQueryMasterId(long broadcastQueryMasterId) {
        this.broadcastQueryMasterId = broadcastQueryMasterId;
    }

    private String localQueryMasterId;

    @Column(name = "LOCAL_QUERY_MASTER_ID")
    @Basic
    public String getLocalQueryMasterId() {
        return localQueryMasterId;
    }

    public void setLocalQueryMasterId(String localQueryMasterId) {
        this.localQueryMasterId = localQueryMasterId;
    }

    private String queryDefinition;

    @Column(name = "QUERY_DEFINITION",length = 32000)
    @Basic
    public String getQueryDefinition() {
        return queryDefinition;
    }

    public void setQueryDefinition(String queryDefinition) {
        this.queryDefinition = queryDefinition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MasterQueryEntity that = (MasterQueryEntity) o;

        if (broadcastQueryMasterId != that.broadcastQueryMasterId) return false;
        if (localQueryMasterId != null ? !localQueryMasterId.equals(that.localQueryMasterId) : that.localQueryMasterId != null)
            return false;
        if (queryDefinition != null ? !queryDefinition.equals(that.queryDefinition) : that.queryDefinition != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (broadcastQueryMasterId ^ (broadcastQueryMasterId >>> 32));
        result = 31 * result + (localQueryMasterId != null ? localQueryMasterId.hashCode() : 0);
        result = 31 * result + (queryDefinition != null ? queryDefinition.hashCode() : 0);
        return result;
    }
}
