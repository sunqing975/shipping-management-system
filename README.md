## Running the sample

The Fabric test network is used to deploy and run this sample. Follow these steps in order:

1. Create the test network and a channel (from the `test-network` folder).
   ```
   sudo ./network.sh up createChannel -c mychannel -ca
   ```

1. Deploy one of the smart contract implementations (from the `test-network` folder).
   ```
   # To deploy the Go chaincode implementation
   sudo ./network.sh deployCC -ccn basic -ccp ../asset-transfer-basic/chaincode-go/ -ccl go

   # To deploy the Java chaincode implementation
   sudo ./network.sh deployCC -ccn basic -ccp ../asset-transfer-basic/chaincode-java/ -ccl java
   
   sudo ./network.sh deployCC -ccn basic -ccp ../shipping-management-system/chaincode-go/ -ccl go
   
   sudo ./network.sh deployCC -ccn basic -ccp ../shipping-management-system/chaincode-java/ -ccl java
   ```

1. Run the application (from the `asset-transfer-basic/shipping-management-system` folder).
   ```
   # To run the Go sample application
   cd ../asset-transfer-basic/application-gateway-go
   go run .

   # To run the Java sample application
   cd ../asset-transfer-basic/application-gateway-java
   sudo ./gradlew run
   
   cd ../shipping-management-system/application-gateway-java
   sudo ./gradlew clean
   sudo ./gradlew build
   cp build/libs/asset-transfer-basic-1.0.0.jar ./
   java -jar asset-transfer-basic-1.0.0.jar
   
   rm -f asset-transfer-basic-1.0.0.jar
   sudo ./gradlew clean
   ```

## Clean up

When you are finished, you can bring down the test network (from the `test-network` folder). The command will remove all
the nodes of the test network, and delete any ledger data that you created.

```
./network.sh down
```