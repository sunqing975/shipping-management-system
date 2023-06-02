package com.shipping.entity;

/**
 * @className: com.shipping.entity.ECPolicy
 * @author: Superman
 * @create: 2023-05-17 21:40
 * @description: TODO
 */
public class ECPolicy {
    private String ID;
    private String EcId;
    private String Policy;

    public ECPolicy(String ID, String ecId, String policy) {
        this.ID = ID;
        EcId = ecId;
        Policy = policy;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getEcId() {
        return EcId;
    }

    public void setEcId(String ecId) {
        EcId = ecId;
    }

    public String getPolicy() {
        return Policy;
    }

    public void setPolicy(String policy) {
        Policy = policy;
    }

    @Override
    public String toString() {
        return "ECPolicy{" +
                "ID='" + ID + '\'' +
                ", EcId='" + EcId + '\'' +
                ", Policy='" + Policy + '\'' +
                '}';
    }
}
