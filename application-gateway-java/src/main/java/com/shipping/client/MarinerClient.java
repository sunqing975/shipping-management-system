package com.shipping.client;/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shipping.entity.User;
import com.shipping.utils.JsonUtils;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.identity.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class MarinerClient {
    private static final String MSP_ID = System.getenv().getOrDefault("MSP_ID", "Org1MSP");
    private static final String CHANNEL_NAME = System.getenv().getOrDefault("CHANNEL_NAME", "mychannel");
    private static final String CHAINCODE_NAME = System.getenv().getOrDefault("CHAINCODE_NAME", "basic");

    // Path to crypto materials.
    private static final Path CRYPTO_PATH = Paths.get("../../test-network/organizations/peerOrganizations/org1.example.com");
    // Path to user certificate.
    private static final Path CERT_PATH = CRYPTO_PATH.resolve(Paths.get("users/User1@org1.example.com/msp/signcerts/cert.pem"));
    // Path to user private key directory.
    private static final Path KEY_DIR_PATH = CRYPTO_PATH.resolve(Paths.get("users/User1@org1.example.com/msp/keystore"));
    // Path to peer tls certificate.
    private static final Path TLS_CERT_PATH = CRYPTO_PATH.resolve(Paths.get("peers/peer0.org1.example.com/tls/ca.crt"));

    // Gateway peer end point.
    private static final String PEER_ENDPOINT = "localhost:7051";
    private static final String OVERRIDE_AUTH = "peer0.org1.example.com";

    public static void main(final String[] args) throws Exception {
        MarinerClient client = new MarinerClient();
        ManagedChannel channel = client.newGrpcConnection();
        Contract userContract = client.getContract(channel, "UserContract");
        userContract.submitTransaction("initLedger");
        Contract attributeContract = client.getContract(channel, "AttributeContract");
        attributeContract.submitTransaction("initLedger");
        Contract ecPolicyContract = client.getContract(channel, "ECPolicyContract");
        ecPolicyContract.submitTransaction("initLedger");
        Contract energyConsumptionContract = client.getContract(channel, "EnergyConsumptionContract");
        energyConsumptionContract.submitTransaction("initLedger");
        Contract userAttributeContract = client.getContract(channel, "UserAttributeContract");
        userAttributeContract.submitTransaction("initLedger");
        System.out.println("链码执行完成");

        List<User> allUsers;

        byte[] result = userContract.evaluateTransaction("getAllUsers");
        String json = JsonUtils.prettyJson(result);
        Type type = new TypeToken<List<User>>() {
        }.getType();
        Gson gson = new Gson();
        allUsers = gson.fromJson(json, type);
        System.out.println(allUsers);
        client.closeChannel(channel);
    }

    public void closeChannel(ManagedChannel channel) {
        try {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Contract getContract(ManagedChannel channel, String contractName) {
        Gateway.Builder builder;
        try {
            builder = Gateway.newInstance().identity(newIdentity()).signer(newSigner()).connection(channel)
                    // Default timeouts for different gRPC calls
                    .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                    .endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
                    .submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                    .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));
        } catch (IOException | CertificateException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        Gateway gateway = builder.connect();
        var network = gateway.getNetwork(CHANNEL_NAME);
        // Get the smart contract from the network.
        return network.getContract(CHAINCODE_NAME, contractName);
    }


    public ManagedChannel newGrpcConnection() throws IOException {
        var credentials = TlsChannelCredentials.newBuilder()
                .trustManager(TLS_CERT_PATH.toFile())
                .build();
        return Grpc.newChannelBuilder(PEER_ENDPOINT, credentials)
                .overrideAuthority(OVERRIDE_AUTH)
                .build();
    }

    private static Identity newIdentity() throws IOException, CertificateException {
        var certReader = Files.newBufferedReader(CERT_PATH);
        var certificate = Identities.readX509Certificate(certReader);

        return new X509Identity(MSP_ID, certificate);
    }

    private static Signer newSigner() throws IOException, InvalidKeyException {
        var keyReader = Files.newBufferedReader(getPrivateKeyPath());
        var privateKey = Identities.readPrivateKey(keyReader);

        return Signers.newPrivateKeySigner(privateKey);
    }

    private static Path getPrivateKeyPath() throws IOException {
        try (var keyFiles = Files.list(KEY_DIR_PATH)) {
            return keyFiles.findFirst().orElseThrow();
        }
    }

}
