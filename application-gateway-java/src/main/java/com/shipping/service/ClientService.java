package com.shipping.service;

import com.shipping.entity.Attribute;
import com.shipping.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientService {

    public String login(List<User> users, String username, int role) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getRole() == role) {
                return user.getId();
            }
        }
        return null;
    }

    public boolean attributeCheck(List<Attribute> userAttrs, String policy) {
        return true;
    }
}
