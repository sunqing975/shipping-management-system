package com.shipping;

/**
 * @className: com.shipping.ECPolicy
 * @author: Superman
 * @create: 2023-05-17 21:40
 * @description: TODO
 */
public class ECPolicy {
    private String id;
    private String ecId;
    private String policy;

    public ECPolicy(String id, String ecId, String policy) {
        this.id = id;
        this.ecId = ecId;
        this.policy = policy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEcId() {
        return ecId;
    }

    public void setEcId(String ecId) {
        this.ecId = ecId;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }
}
