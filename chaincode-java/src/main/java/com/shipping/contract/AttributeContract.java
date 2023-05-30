/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.shipping.contract;

import com.owlike.genson.Genson;
import com.shipping.entity.Attribute;
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
@Contract(name = "AttributeContract")
public final class AttributeContract implements ContractInterface {

    private final Genson genson = new Genson();

    private enum AttributeContractErrors {
        /**
         * 属性不存在
         */
        ATTRIBUTE_NOT_FOUND,
        /**
         * 属性信息已经存在
         */
        ATTRIBUTE_ALREADY_EXISTS
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        createAttribute(ctx, "att1", "a1", "v1");
        createAttribute(ctx, "att12", "a1", "v12");
        createAttribute(ctx, "att2", "a2", "v2");
        createAttribute(ctx, "att3", "a3", "v3");
        createAttribute(ctx, "att4", "a4", "v4");
        createAttribute(ctx, "att5", "a5", "v5");
        createAttribute(ctx, "att6", "a6", "v6");
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Attribute createAttribute(final Context ctx, final String id, final String name, final String value) {
        ChaincodeStub stub = ctx.getStub();
        if (attributeExists(ctx, id)) {
            String errorMessage = String.format("Attribute %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AttributeContractErrors.ATTRIBUTE_ALREADY_EXISTS.toString());
        }
        Attribute attribute = new Attribute(id, name, value);
        // Use Genson to convert the Attribute into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(attribute);
        stub.putStringState(id, sortedJson);
        return attribute;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Attribute readAttribute(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String attributeJson = stub.getStringState(id);

        if (attributeJson == null || attributeJson.isEmpty()) {
            String errorMessage = String.format("Attribute %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AttributeContractErrors.ATTRIBUTE_NOT_FOUND.toString());
        }
        return genson.deserialize(attributeJson, Attribute.class);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Attribute updateAttribute(final Context ctx, final String id, final String name, final String value) {
        ChaincodeStub stub = ctx.getStub();

        if (!attributeExists(ctx, id)) {
            String errorMessage = String.format("Attribute %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AttributeContractErrors.ATTRIBUTE_NOT_FOUND.toString());
        }

        Attribute newAttribute = new Attribute(id, name, value);
        // Use Genson to convert the Attribute into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(newAttribute);
        stub.putStringState(id, sortedJson);
        return newAttribute;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void deleteAttribute(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();

        if (!attributeExists(ctx, id)) {
            String errorMessage = String.format("Attribute %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AttributeContractErrors.ATTRIBUTE_NOT_FOUND.toString());
        }

        stub.delState(id);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean attributeExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String attributeJson = stub.getStringState(id);

        return (attributeJson != null && !attributeJson.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAllAttributes(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Attribute> queryResults = new ArrayList<>();
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");
        for (KeyValue result : results) {
            Attribute attribute = genson.deserialize(result.getStringValue(), Attribute.class);
            System.out.println(attribute);
            queryResults.add(attribute);
        }

        return genson.serialize(queryResults);
    }
}
