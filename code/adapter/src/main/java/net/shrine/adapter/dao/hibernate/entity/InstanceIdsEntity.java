package net.shrine.adapter.dao.hibernate.entity;

import javax.persistence.*;


@Table(name = "INSTANCE_IDS")
@Entity
public class InstanceIdsEntity {
    private long broadcastQueryInstanceId;

    @Column(name = "BROADCAST_QUERY_INSTANCE_ID")
    @Id
    public long getBroadcastQueryInstanceId() {
        return broadcastQueryInstanceId;
    }

    public void setBroadcastQueryInstanceId(long broadcastQueryInstanceId) {
        this.broadcastQueryInstanceId = broadcastQueryInstanceId;
    }

    private String localQueryInstanceId;

    @Column(name = "LOCAL_QUERY_INSTANCE_ID")
    @Basic
    public String getLocalQueryInstanceId() {
        return localQueryInstanceId;
    }

    public void setLocalQueryInstanceId(String localQueryInstanceId) {
        this.localQueryInstanceId = localQueryInstanceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstanceIdsEntity that = (InstanceIdsEntity) o;

        if (broadcastQueryInstanceId != that.broadcastQueryInstanceId) return false;
        if (localQueryInstanceId != null ? !localQueryInstanceId.equals(that.localQueryInstanceId) : that.localQueryInstanceId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (broadcastQueryInstanceId ^ (broadcastQueryInstanceId >>> 32));
        result = 31 * result + (localQueryInstanceId != null ? localQueryInstanceId.hashCode() : 0);
        return result;
    }
}
