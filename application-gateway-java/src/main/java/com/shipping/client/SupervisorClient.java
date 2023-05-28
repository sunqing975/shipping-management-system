package com.shipping.client;/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.shipping.entity.User;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import org.hyperledger.fabric.client.CommitException;
import org.hyperledger.fabric.client.CommitStatusException;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.EndorseException;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.SubmitException;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Identity;
import org.hyperledger.fabric.client.identity.Signer;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class SupervisorClient {
    private static final String MSP_ID = System.getenv().getOrDefault("MSP_ID", "Org2MSP");
    private static final String CHANNEL_NAME = System.getenv().getOrDefault("CHANNEL_NAME", "mychannel");
    private static final String CHAINCODE_NAME = System.getenv().getOrDefault("CHAINCODE_NAME", "basic");

    // Path to crypto materials.
    private static final Path CRYPTO_PATH = Paths.get("../../test-network/organizations/peerOrganizations/org2.example.com");
    // Path to user certificate.
    private static final Path CERT_PATH = CRYPTO_PATH.resolve(Paths.get("users/User2@org1.example.com/msp/signcerts/cert.pem"));
    // Path to user private key directory.
    private static final Path KEY_DIR_PATH = CRYPTO_PATH.resolve(Paths.get("users/User2@org1.example.com/msp/keystore"));
    // Path to peer tls certificate.
    private static final Path TLS_CERT_PATH = CRYPTO_PATH.resolve(Paths.get("peers/peer0.org2.example.com/tls/ca.crt"));

    // Gateway peer end point.
    private static final String PEER_ENDPOINT = "localhost:9051";
    private static final String OVERRIDE_AUTH = "peer0.org2.example.com";
    private final String assetId = "asset" + Instant.now().toEpochMilli();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(final String[] args) throws Exception {
        SupervisorClient client = new SupervisorClient();
        ManagedChannel channel = client.newGrpcConnection();
        Contract contract = client.getContract(channel);
        client.initLedger(contract);
        client.closeChannel(channel);

    }

    public void closeChannel(ManagedChannel channel) {
        try {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Contract getContract(ManagedChannel channel) {
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
        return network.getContract(CHAINCODE_NAME);
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

    /**
     * This type of transaction would typically only be run once by an application
     * the first time it was started after its initial deployment. A new version of
     * the chaincode deployed later would likely not need to run an "init" function.
     */
    public void initLedger(Contract contract) throws EndorseException, SubmitException, CommitStatusException, CommitException {
        System.out.println("\n--> Submit Transaction: InitLedger, function creates the initial set of assets on the ledger");

        contract.submitTransaction("InitLedger");

        System.out.println("*** Transaction committed successfully");
    }

    /**
     * Evaluate a transaction to query ledger state.
     */
    public List<User> getAllUsers(Contract contract) throws GatewayException {
        System.out.println("\n--> Evaluate Transaction: GetAllAssets, function returns all the current assets on the ledger");

        var result = contract.evaluateTransaction("getAllUsers");
        String json = prettyJson(result);

        System.out.println("*** Result: " + json);



        return null;
    }

    private String prettyJson(final byte[] json) {
        return prettyJson(new String(json, StandardCharsets.UTF_8));
    }

    private String prettyJson(final String json) {
        var parsedJson = JsonParser.parseString(json);
        return gson.toJson(parsedJson);
    }

    /**
     * Submit a transaction synchronously, blocking until it has been committed to
     * the ledger.
     */
    public void createAsset(Contract contract) throws EndorseException, SubmitException, CommitStatusException, CommitException {
        System.out.println("\n--> Submit Transaction: CreateAsset, creates new asset with ID, Color, Size, Owner and AppraisedValue arguments");

        contract.submitTransaction("CreateAsset", assetId, "yellow", "5", "Tom", "1300");

        System.out.println("*** Transaction committed successfully");
    }

    /**
     * Submit transaction asynchronously, allowing the application to process the
     * smart contract response (e.g. update a UI) while waiting for the commit
     * notification.
     */
    public void transferAssetAsync(Contract contract) throws EndorseException, SubmitException, CommitStatusException {
        System.out.println("\n--> Async Submit Transaction: TransferAsset, updates existing asset owner");

        var commit = contract.newProposal("TransferAsset")
                .addArguments(assetId, "Saptha")
                .build()
                .endorse()
                .submitAsync();

        var result = commit.getResult();
        var oldOwner = new String(result, StandardCharsets.UTF_8);

        System.out.println("*** Successfully submitted transaction to transfer ownership from " + oldOwner + " to Saptha");
        System.out.println("*** Waiting for transaction commit");

        var status = commit.getStatus();
        if (!status.isSuccessful()) {
            throw new RuntimeException("Transaction " + status.getTransactionId() +
                    " failed to commit with status code " + status.getCode());
        }

        System.out.println("*** Transaction committed successfully");
    }

    public void readAssetById(Contract contract) throws GatewayException {
        System.out.println("\n--> Evaluate Transaction: ReadAsset, function returns asset attributes");

        var evaluateResult = contract.evaluateTransaction("ReadAsset", assetId);

        System.out.println("*** Result:" + prettyJson(evaluateResult));
    }

    /**
     * submitTransaction() will throw an error containing details of any error
     * responses from the smart contract.
     */
    public void updateNonExistentAsset(Contract contract) {
        try {
            System.out.println("\n--> Submit Transaction: UpdateAsset asset70, asset70 does not exist and should return an error");

            contract.submitTransaction("UpdateAsset", "asset70", "blue", "5", "Tomoko", "300");

            System.out.println("******** FAILED to return an error");
        } catch (EndorseException | SubmitException | CommitStatusException e) {
            System.out.println("*** Successfully caught the error: ");
            e.printStackTrace(System.out);
            System.out.println("Transaction ID: " + e.getTransactionId());

            var details = e.getDetails();
            if (!details.isEmpty()) {
                System.out.println("Error Details:");
                for (var detail : details) {
                    System.out.println("- address: " + detail.getAddress() + ", mspId: " + detail.getMspId()
                            + ", message: " + detail.getMessage());
                }
            }
        } catch (CommitException e) {
            System.out.println("*** Successfully caught the error: " + e);
            e.printStackTrace(System.out);
            System.out.println("Transaction ID: " + e.getTransactionId());
            System.out.println("Status code: " + e.getCode());
        }
    }
}
