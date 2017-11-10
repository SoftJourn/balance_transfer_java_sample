package com.softjourn.balance.transfer.client.properties;

import lombok.Data;

import java.util.List;

@Data
public class Organization {

    private String name;

    private List<Peer> peers;

    private List<User> users;

}
