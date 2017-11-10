package com.softjourn.balance.transfer.client.properties;

import lombok.Data;

@Data
public class Orderer {

    private String name;

    private String url;

    private String certificate;

}
