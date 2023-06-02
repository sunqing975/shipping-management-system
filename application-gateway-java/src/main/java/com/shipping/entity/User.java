package com.shipping.entity;

/**
 * @className: com.shipping.entity.User
 * @author: Superman
 * @create: 2023-05-17 21:38
 * @description: TODO
 */
public class User {
    private String ID;
    private String Username;
    private Integer Role;

    public User(String ID, String username, Integer role) {
        this.ID = ID;
        Username = username;
        Role = role;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        ID = ID;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public Integer getRole() {
        return Role;
    }

    public void setRole(Integer role) {
        Role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "ID='" + ID + '\'' +
                ", Username='" + Username + '\'' +
                ", Role=" + Role +
                '}';
    }
}
