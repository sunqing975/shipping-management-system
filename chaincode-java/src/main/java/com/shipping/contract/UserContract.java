/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.shipping.contract;

import com.owlike.genson.Genson;
import com.shipping.constant.RoleConst;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import com.shipping.entity.User;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Superman
 */
@Contract(name = "UserContract")
@Default
public final class UserContract implements ContractInterface {

    private final Genson genson = new Genson();

    private enum UserContractErrors {
        /**
         * 用户不存在
         */
        USER_NOT_FOUND,
        /**
         * 用户信息已经存在
         */
        USER_ALREADY_EXISTS
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        createUser(ctx, "user1", "admin", RoleConst.SUPERVISOR.getRole());
        createUser(ctx, "user2", "zhangsan", RoleConst.MARINER.getRole());
        createUser(ctx, "user3", "lisi", RoleConst.MARINER.getRole());
        createUser(ctx, "user4", "wangwu", RoleConst.MARINER.getRole());
        createUser(ctx, "user5", "zhaoliu", RoleConst.MARINER.getRole());
        createUser(ctx, "user6", "mazi", RoleConst.MARINER.getRole());
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public User createUser(final Context ctx, final String id, final String username, final Integer role) {
        ChaincodeStub stub = ctx.getStub();
        if (userExists(ctx, id)) {
            String errorMessage = String.format("User %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserContractErrors.USER_ALREADY_EXISTS.toString());
        }
        User user = new User(id, username, role);
        // Use Genson to convert the User into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(user);
        stub.putStringState(id, sortedJson);
        return user;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public User readUser(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String userJson = stub.getStringState(id);

        if (userJson == null || userJson.isEmpty()) {
            String errorMessage = String.format("User %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserContractErrors.USER_NOT_FOUND.toString());
        }
        return genson.deserialize(userJson, User.class);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public User updateUser(final Context ctx, final String id, final String username, final Integer role) {
        ChaincodeStub stub = ctx.getStub();

        if (!userExists(ctx, id)) {
            String errorMessage = String.format("User %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserContractErrors.USER_NOT_FOUND.toString());
        }

        User newUser = new User(id, username, role);
        // Use Genson to convert the User into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(newUser);
        stub.putStringState(id, sortedJson);
        return newUser;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void deleteUser(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();

        if (!userExists(ctx, id)) {
            String errorMessage = String.format("User %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserContractErrors.USER_NOT_FOUND.toString());
        }

        stub.delState(id);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean userExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String userJson = stub.getStringState(id);

        return (userJson != null && !userJson.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAllUsers(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<User> queryResults = new ArrayList<>();
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");
        for (KeyValue result : results) {
            User user = genson.deserialize(result.getStringValue(), User.class);
            System.out.println(user);
            queryResults.add(user);
        }

        return genson.serialize(queryResults);
    }
}
