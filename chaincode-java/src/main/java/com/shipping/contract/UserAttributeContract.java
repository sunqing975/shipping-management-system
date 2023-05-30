/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.shipping.contract;

import com.owlike.genson.Genson;
import com.shipping.entity.UserAttribute;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Superman
 */
@Contract(name = "UserAttributeContract")
public final class UserAttributeContract implements ContractInterface {

    private final Genson genson = new Genson();

    private enum UserAttributeContractErrors {
        /**
         * 属性不存在
         */
        USERATTRIBUTE_NOT_FOUND,
        /**
         * 属性信息已经存在
         */
        USERATTRIBUTE_ALREADY_EXISTS
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        createUserAttribute(ctx, "ua1", "a1", "v1");
        createUserAttribute(ctx, "ua2", "a2", "v2");
        createUserAttribute(ctx, "ua3", "a3", "v3");
        createUserAttribute(ctx, "ua4", "a4", "v4");
        createUserAttribute(ctx, "ua5", "a5", "v5");
        createUserAttribute(ctx, "ua6", "a6", "v6");
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public UserAttribute createUserAttribute(final Context ctx, final String id, final String userId, final String attributeId) {
        ChaincodeStub stub = ctx.getStub();
        if (userAttributeExists(ctx, id)) {
            String errorMessage = String.format("UserAttribute %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserAttributeContractErrors.USERATTRIBUTE_ALREADY_EXISTS.toString());
        }
        UserAttribute userAttribute = new UserAttribute(id, userId, attributeId);
        // Use Genson to convert the UserAttribute into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(userAttribute);
        stub.putStringState(id, sortedJson);
        return userAttribute;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public UserAttribute readUserAttribute(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String userAttributeJson = stub.getStringState(id);

        if (userAttributeJson == null || userAttributeJson.isEmpty()) {
            String errorMessage = String.format("UserAttribute %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserAttributeContractErrors.USERATTRIBUTE_NOT_FOUND.toString());
        }
        return genson.deserialize(userAttributeJson, UserAttribute.class);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public UserAttribute updateUserAttribute(final Context ctx, final String id, final String userId,
                                             final String attributeId) {
        ChaincodeStub stub = ctx.getStub();

        if (!userAttributeExists(ctx, id)) {
            String errorMessage = String.format("UserAttribute %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserAttributeContractErrors.USERATTRIBUTE_NOT_FOUND.toString());
        }

        UserAttribute newUserAttribute = new UserAttribute(id, userId, attributeId);
        // Use Genson to convert the UserAttribute into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(newUserAttribute);
        stub.putStringState(id, sortedJson);
        return newUserAttribute;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void deleteUserAttribute(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();

        if (!userAttributeExists(ctx, id)) {
            String errorMessage = String.format("UserAttribute %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UserAttributeContractErrors.USERATTRIBUTE_NOT_FOUND.toString());
        }

        stub.delState(id);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean userAttributeExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String userAttributeJson = stub.getStringState(id);

        return (userAttributeJson != null && !userAttributeJson.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAllUserAttributes(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<UserAttribute> queryResults = new ArrayList<>();
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");
        for (KeyValue result : results) {
            UserAttribute userAttribute = genson.deserialize(result.getStringValue(), UserAttribute.class);
            System.out.println(userAttribute);
            queryResults.add(userAttribute);
        }

        return genson.serialize(queryResults);
    }
}
