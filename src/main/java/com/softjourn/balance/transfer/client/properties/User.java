package com.softjourn.balance.transfer.client.properties;

import lombok.Data;

@Data
public class User {

    private String name;

    private String role;

    private String msp;

    private String certificate;

    private String privateKey;

    public boolean isAdmin() {
        return this.role.equals("ADMIN");
    }

}
