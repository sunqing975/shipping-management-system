/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.shipping;

import com.owlike.genson.Genson;
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
@Contract(name = "EnergyConsumptionContract")
public final class EnergyConsumptionContract implements ContractInterface {

    private final Genson genson = new Genson();

    private enum EnergyConsumptionContractErrors {
        /**
         * 属性不存在
         */
        ENERGY_CONSUMPTION_NOT_FOUND,
        /**
         * 属性信息已经存在
         */
        ENERGY_CONSUMPTION_ALREADY_EXISTS
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        createEnergyConsumption(ctx, "ec1", 100, 1, "ship1", "large", 53.5, 0.0,
                "岸电", 12.5, "kw.h", "user1", 1684422876L, 1684422901L);
        createEnergyConsumption(ctx, "ec2", 100, 2, "ship2", "small", 13.5, 0.2,
                "柴油/汽油（MDO/MGO）", 4.0, "吨", "user1", 1684422876L, 1684422901L);
        createEnergyConsumption(ctx, "ec3", 101, 3, "ship3", "medium", 33.5, 2.2,
                "轻燃油（LFO） 硫含量高于0.1% m/m，但不高于0.5% m/m", 181.19, "吨", "user2", 1684422876L, 1684422901L);
        createEnergyConsumption(ctx, "ec4", 101, 4, "ship4", "large", 73.6, 0.2,
                "岸电", 12.5, "kw.h", "user2", 1684422876L, 1684422901L);
        createEnergyConsumption(ctx, "ec5", 102, 5, "ship5", "medium", 43.6, 0.2,
                "岸电", 12.5, "kw.h", "user3", 1684422876L, 1684422901L);
        createEnergyConsumption(ctx, "ec6", 102, 6, "ship6", "large", 63.5, 0.2,
                "重燃油（HFO） 硫含量高于0.5% m/m", 519.75, "吨", "user3", 1684422876L, 1684422901L);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public EnergyConsumption createEnergyConsumption(final Context ctx, final String id, final Integer orgCode,
                                                     final Integer shipId,
                                                     final String shipName, final String shipType,
                                                     final Double shipSize, final Double energyType,
                                                     final String energyName, final Double consumeQuantity,
                                                     final String consumeQuantityUnit, final String operatorId,
                                                     final Long startTime, final Long endTime) {
        ChaincodeStub stub = ctx.getStub();
        if (energyConsumptionExists(ctx, id)) {
            String errorMessage = String.format("EnergyConsumption %s already exists", id);
            System.out.println(errorMessage);
//            throw new ChaincodeException(errorMessage,
//                    EnergyConsumptionContractErrors.ENERGY_CONSUMPTION_ALREADY_EXISTS.toString());
        }
        EnergyConsumption energyConsumption = new EnergyConsumption(id, orgCode, shipId,
                shipName, shipType,
                shipSize, energyType,
                energyName, consumeQuantity,
                consumeQuantityUnit, operatorId,
                startTime, endTime);
        // Use Genson to convert the EnergyConsumption into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(energyConsumption);
        stub.putStringState(id, sortedJson);
        return energyConsumption;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public EnergyConsumption readEnergyConsumption(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String energyConsumptionJson = stub.getStringState(id);

        if (energyConsumptionJson == null || energyConsumptionJson.isEmpty()) {
            String errorMessage = String.format("EnergyConsumption %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, EnergyConsumptionContractErrors.ENERGY_CONSUMPTION_NOT_FOUND.toString());
        }
        return genson.deserialize(energyConsumptionJson, EnergyConsumption.class);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public EnergyConsumption updateEnergyConsumption(final Context ctx, final String id, final Integer orgCode,
                                                     final Integer shipId,
                                                     final String shipName, final String shipType,
                                                     final Double shipSize, final Double energyType,
                                                     final String energyName, final Double consumeQuantity,
                                                     final String consumeQuantityUnit, final String operatorId,
                                                     final Long startTime, final Long endTime) {
        ChaincodeStub stub = ctx.getStub();

        if (!energyConsumptionExists(ctx, id)) {
            String errorMessage = String.format("EnergyConsumption %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, EnergyConsumptionContractErrors.ENERGY_CONSUMPTION_NOT_FOUND.toString());
        }

        EnergyConsumption newEnergyConsumption = new EnergyConsumption(id, orgCode, shipId,
                shipName, shipType,
                shipSize, energyType,
                energyName, consumeQuantity,
                consumeQuantityUnit, operatorId,
                startTime, endTime);
        // Use Genson to convert the EnergyConsumption into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(newEnergyConsumption);
        stub.putStringState(id, sortedJson);
        return newEnergyConsumption;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void deleteEnergyConsumption(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();

        if (!energyConsumptionExists(ctx, id)) {
            String errorMessage = String.format("EnergyConsumption %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, EnergyConsumptionContractErrors.ENERGY_CONSUMPTION_NOT_FOUND.toString());
        }

        stub.delState(id);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean energyConsumptionExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String energyConsumptionJson = stub.getStringState(id);

        return (energyConsumptionJson != null && !energyConsumptionJson.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAllEnergyConsumptions(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<EnergyConsumption> queryResults = new ArrayList<>();
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");
        for (KeyValue result : results) {
            EnergyConsumption energyConsumption = genson.deserialize(result.getStringValue(), EnergyConsumption.class);
            System.out.println(energyConsumption);
            queryResults.add(energyConsumption);
        }

        return genson.serialize(queryResults);
    }
}
