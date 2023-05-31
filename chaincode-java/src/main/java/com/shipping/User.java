package com.shipping;

/**
 * @className: com.shipping.User
 * @author: Superman
 * @create: 2023-05-17 21:38
 * @description: TODO
 */
public class User {
    private String id;
    private String username;
    private Integer role;

    public User(String id, String username, Integer role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }
}
