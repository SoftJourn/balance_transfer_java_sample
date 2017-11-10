package com.softjourn.balance.transfer.client.dto;

import lombok.Data;

@Data
public class InitializeChaincodeDTO extends InstallChaincodeDTO {

    private String[] args;

}
