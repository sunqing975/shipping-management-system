package com.shipping.entity;

/**
 * @className: com.shipping.entity.UserAttribute
 * @author: Superman
 * @create: 2023-05-17 21:39
 * @description: TODO
 */
public class UserAttribute {
    private String id;
    private String userId;
    private String attributeId;

    public UserAttribute(String id, String userId, String attributeId) {
        this.id = id;
        this.userId = userId;
        this.attributeId = attributeId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }
}
