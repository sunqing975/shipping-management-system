package com.shipping.service;

import com.shipping.entity.User;

import java.util.List;

public class ClientService {

    public Boolean login(List<User> users, String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
