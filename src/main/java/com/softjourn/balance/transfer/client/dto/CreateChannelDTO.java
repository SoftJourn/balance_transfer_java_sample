package com.softjourn.balance.transfer.client.dto;

import lombok.Data;

@Data
public class CreateChannelDTO {

    private String channelName;

    private String organization;

    private String[] peers;

}
