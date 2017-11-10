package com.softjourn.balance.transfer.client.dto;

import lombok.Data;

@Data
public class InstallChaincodeDTO extends CreateChannelDTO {

    private String chaincodeName;

}
