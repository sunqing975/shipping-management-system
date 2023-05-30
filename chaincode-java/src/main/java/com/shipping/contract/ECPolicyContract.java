/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.shipping.contract;

import com.owlike.genson.Genson;
import com.shipping.entity.ECPolicy;
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
@Contract(name = "ECPolicyContract")
public final class ECPolicyContract implements ContractInterface {

    private final Genson genson = new Genson();

    private enum ECPolicyContractErrors {
        /**
         * 策略不存在
         */
        ECPOLICY_NOT_FOUND,
        /**
         * 策略信息已经存在
         */
        ECPOLICY_ALREADY_EXISTS
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        createECPolicy(ctx, "ecpolicy1", "ec1", "att1 att2 att3 2of3 att4 1of2");
        createECPolicy(ctx, "ecpolicy2", "ec2", "att1 att2 att3 2of3 att4 1of2");
        createECPolicy(ctx, "ecpolicy3", "ec3", "att1 att2 att3 2of3 att4 1of2");
        createECPolicy(ctx, "ecpolicy4", "ec4", "att1 att2 att3 2of3 att4 1of2");
        createECPolicy(ctx, "ecpolicy5", "ec5", "att1 att2 att3 2of3 att4 1of2");
        createECPolicy(ctx, "ecpolicy6", "ec6", "att1 att2 att3 2of3 att4 1of2");
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public ECPolicy createECPolicy(final Context ctx, final String id, final String ecId, final String policy) {
        ChaincodeStub stub = ctx.getStub();
        if (policyExists(ctx, id)) {
            String errorMessage = String.format("ECPolicy %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, ECPolicyContractErrors.ECPOLICY_ALREADY_EXISTS.toString());
        }
        ECPolicy ecpolicy = new ECPolicy(id, ecId, policy);
        // Use Genson to convert the ECPolicy into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(ecpolicy);
        stub.putStringState(id, sortedJson);
        return ecpolicy;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public ECPolicy readECPolicy(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String policyJson = stub.getStringState(id);

        if (policyJson == null || policyJson.isEmpty()) {
            String errorMessage = String.format("ECPolicy %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, ECPolicyContractErrors.ECPOLICY_NOT_FOUND.toString());
        }
        return genson.deserialize(policyJson, ECPolicy.class);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public ECPolicy updateECPolicy(final Context ctx, final String id, final String ecId, final String policy) {
        ChaincodeStub stub = ctx.getStub();

        if (!policyExists(ctx, id)) {
            String errorMessage = String.format("ECPolicy %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, ECPolicyContractErrors.ECPOLICY_NOT_FOUND.toString());
        }

        ECPolicy newECPolicy = new ECPolicy(id, ecId, policy);
        // Use Genson to convert the ECPolicy into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(newECPolicy);
        stub.putStringState(id, sortedJson);
        return newECPolicy;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void deleteECPolicy(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();

        if (!policyExists(ctx, id)) {
            String errorMessage = String.format("ECPolicy %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, ECPolicyContractErrors.ECPOLICY_NOT_FOUND.toString());
        }

        stub.delState(id);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean policyExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String policyJson = stub.getStringState(id);

        return (policyJson != null && !policyJson.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAllECPolicys(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<ECPolicy> queryResults = new ArrayList<>();
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");
        for (KeyValue result : results) {
            ECPolicy policy = genson.deserialize(result.getStringValue(), ECPolicy.class);
            System.out.println(policy);
            queryResults.add(policy);
        }

        return genson.serialize(queryResults);
    }
}
