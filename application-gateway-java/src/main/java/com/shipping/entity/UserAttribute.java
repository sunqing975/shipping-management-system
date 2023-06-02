package com.shipping.entity;

/**
 * @className: com.shipping.entity.UserAttribute
 * @author: Superman
 * @create: 2023-05-17 21:39
 * @description: TODO
 */
public class UserAttribute {
    private String ID;
    private String UserId;
    private String AttId;

    public UserAttribute(String ID, String userId, String attributeId) {
        this.ID = ID;
        UserId = userId;
        AttId = attributeId;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getAttId() {
        return AttId;
    }

    public void setAttId(String attId) {
        AttId = attId;
    }

    @Override
    public String toString() {
        return "UserAttribute{" +
                "ID='" + ID + '\'' +
                ", UserId='" + UserId + '\'' +
                ", AttId='" + AttId + '\'' +
                '}';
    }
}
