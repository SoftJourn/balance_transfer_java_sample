package com.softjourn.balance.transfer.client.dto;

import lombok.Data;

@Data
public class RequestChaincodeDTO extends InitializeChaincodeDTO {

    private String functionName;

}
