package com.softjourn.balance.transfer.client.properties;

import lombok.Data;

@Data
public class Peer {

    private String name;

    private String url;

    private String event;

    private String certificate;

}
