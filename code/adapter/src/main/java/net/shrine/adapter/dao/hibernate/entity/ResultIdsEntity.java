package net.shrine.adapter.dao.hibernate.entity;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: davidortiz
 * Date: 12/13/10
 * Time: 9:19 AM
 * To change this template use File | Settings | File Templates.
 */
@Table(name = "RESULT_IDS")
@Entity
public class ResultIdsEntity {
    private long broadcastResultInstanceId;

    @Column(name = "BROADCAST_RESULT_INSTANCE_ID")
    @Id
    public long getBroadcastResultInstanceId() {
        return broadcastResultInstanceId;
    }

    public void setBroadcastResultInstanceId(long broadcastResultInstanceId) {
        this.broadcastResultInstanceId = broadcastResultInstanceId;
    }

    private String localResultInstanceId;

    @Column(name = "LOCAL_RESULT_INSTANCE_ID")
    @Basic
    public String getLocalResultInstanceId() {
        return localResultInstanceId;
    }

    public void setLocalResultInstanceId(String localResultInstanceId) {
        this.localResultInstanceId = localResultInstanceId;
    }

    private Integer obfuscationAmount;

    @Column(name = "OBFUSCATION_AMOUNT")
    @Basic
    public Integer getObfuscationAmount() {
        return obfuscationAmount;
    }

    public void setObfuscationAmount(Integer obfuscationAmount) {
        this.obfuscationAmount = obfuscationAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResultIdsEntity that = (ResultIdsEntity) o;

        if (broadcastResultInstanceId != that.broadcastResultInstanceId) return false;
        if (obfuscationAmount != that.obfuscationAmount) return false;
        if (localResultInstanceId != null ? !localResultInstanceId.equals(that.localResultInstanceId) : that.localResultInstanceId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (broadcastResultInstanceId ^ (broadcastResultInstanceId >>> 32));
        result = 31 * result + (localResultInstanceId != null ? localResultInstanceId.hashCode() : 0);
        result = 31 * result + obfuscationAmount;
        return result;
    }
}
