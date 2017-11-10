package com.softjourn.balance.transfer.client.properties;

import org.hyperledger.fabric.sdk.User;

import java.util.List;

public interface ChainNetwork {

    User getAdminByOrganization(String org);

    List<Peer> getPeers(String org);

}
