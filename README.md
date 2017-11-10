# balance_transfer_java_sample

A sample web server using Spring Boot 2.M5 to demonstrate how to communicate with Hyperledger Fabric blockchain network using Hyperledger Fabric Java SDK.

### Prerequisites and setup:

* install java 8;
* install gradle;
* install git to clone this project;
* install docker v.1.12 or higher;

```
open file /src/main/resources/application.yaml
and change paths to certificates and private keys that are suitable your system 
```

Once you have completed the above setup, you will have provisioned a local network with the following docker container configuration:

* 2 CAs
* A one orderer
* 4 peers (2 peers per Org)


#### Artifacts
* Crypto material has been generated using the **cryptogen** tool from Hyperledger Fabric and mounted to all peers, the orderering node and CA containers. More details regarding the cryptogen tool are available [here](http://hyperledger-fabric.readthedocs.io/en/latest/build_network.html#crypto-generator).
* An Orderer genesis block (genesis.block) and channel configuration transaction (mychannel.tx) has been pre generated using the **configtxgen** tool from Hyperledger Fabric and placed within the artifacts folder. More details regarding the configtxgen tool are available [here](http://hyperledger-fabric.readthedocs.io/en/latest/build_network.html#configuration-transaction-generator).

## Running the blockchain network

```
cd balance-transfer

./runApp.sh
```

## Running the web server
```
gradle bootRun
```


## Sample REST APIs Requests

### Login request
```    
curl -X POST http://localhost:8080/oauth/token 
   -H 'content-type: application/x-www-form-urlencoded'
   -d 'grant_type=password&client_id=client&scope=read%20write&username=admin&password=admin'
```   
   
**Response:**   
  
```
{
    "access_token": "1e8687bb-4b3e-4ac3-b0e5-c6b583fc3d71",
    "token_type": "bearer",
    "refresh_token": "18c02496-4633-4c2f-97b6-26c25f53f1be",
    "expires_in": 4999,
    "scope": "read write"
}
```   

### Create channel request
```
curl -X POST http://localhost:8080/channel
   -H 'authorization: bearer 1e8687bb-4b3e-4ac3-b0e5-c6b583fc3d71' 
   -H 'content-type: application/json' 
   -H 'postman-token: 6ec6b006-39b5-072e-a48e-1d1d2e7239bb' 
   -d '{
    "channelName":"mychannel",
    "organization":"org1",
    "peers":["peer0.org1.example.com","peer1.org1.example.com"]
    }'
```
   
**Response:**   
  
```
{
    "Status": "OK",
    "Message": "Channel with channelName mychannel was created successfully"
}
```   

### Install chaincode request
```
curl -X POST http://localhost:8080/chaincode
   -H 'authorization: bearer 1e8687bb-4b3e-4ac3-b0e5-c6b583fc3d71' 
   -H 'content-type: application/json' 
   -d '{
         "channelName":"mychannel",
         "chaincodeName":"example_cc_go",
         "organization":"org1",
         "peers":["peer0.org1.example.com","peer1.org1.example.com"]
       }'
```
   
**Response:**   
  
```
{
    "Status": "OK",
    "Message": "Chaincode with name example_cc_go was installed successfully"
}
```

### Initialize chaincode request
```
curl -X POST http://localhost:8080/chaincode/initialize
   -H 'authorization: bearer 1e8687bb-4b3e-4ac3-b0e5-c6b583fc3d71' 
   -H 'content-type: application/json' 
   -d '{
         "channelName":"mychannel",
         "chaincodeName":"example_cc_go",
         "organization":"org1",
         "peers":["peer0.org1.example.com","peer1.org1.example.com"],
         "args":["a","100","b","200"]
       }'
```
   
**Response:**   
  
```
{
    "Status": "OK",
    "Message": "Chaincode with name example_cc_go was initialized successfully"
}
```

### Perform transaction request
```
curl -X POST http://localhost:8080/chaincode/transaction
   -H 'authorization: bearer 1e8687bb-4b3e-4ac3-b0e5-c6b583fc3d71' 
   -H 'content-type: application/json' 
   -d '{
         "channelName":"mychannel",
         "chaincodeName":"example_cc_go",
         "functionName":"move",
         "organization":"org1",
         "peers":["peer0.org1.example.com","peer1.org1.example.com"],
         "args":["a", "b", "50"]
       }'
```
   
**Response:**   
  
```
{
    "Status": "OK",
    "TransactionID": "3ce9de420fe0141dacabcbfb1926914472e6955933bba606e54124d4c62c910a"
}
```

### Perform query request
```
curl -X POST http://localhost:8080/chaincode/query
   -H 'authorization: bearer 1e8687bb-4b3e-4ac3-b0e5-c6b583fc3d71' 
   -H 'content-type: application/json' 
   -d '{
         "channelName":"mychannel",
         "chaincodeName":"example_cc_go",
         "functionName":"query",
         "organization":"org1",
         "peers":["peer0.org1.example.com","peer1.org1.example.com"],
         "args":["a"]
       }'
```
   
**Response:**   
  
```
{
    "Status": "OK",
    "Result": "50"
}
```

### Request to get blockchain info
```
curl -X GET 'http://localhost:8080/channel?channelName=mychannel&org=org1&peers=peer0.org1.example.com' \
  -H 'authorization: bearer 1e8687bb-4b3e-4ac3-b0e5-c6b583fc3d71'
  -H 'content-type: application/json'
  -d '{
        "name":"mychannel",
        "organization":"org1",
        "peers":["peer.org1.example.com"]
      }'
```
   
**Response:**   
  
```
{
    "Status": "OK",
    "CurrentBlockHash": "KO7SYHmsUrYVUSl1PZKOJShAiShg4bvd/ks6UnFkWuI=",
    "Height": 3,
    "PreviousBlockHash": "MIHSINGpciRb5UfYeEeO+y8CozkZtUbsql+bJDsAKnE="
}
```