package com.softjourn.balance.transfer.client.properties;

import lombok.Data;

@Data
public class Chaincode {

    private String name;

    private String version;

    private String pathToFile;

    private String sourceLocation;

}
