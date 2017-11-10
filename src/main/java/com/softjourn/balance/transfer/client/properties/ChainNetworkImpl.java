package com.softjourn.balance.transfer.client.properties;

import com.softjourn.balance.transfer.client.chainImpl.ChainUser;
import com.softjourn.balance.transfer.client.exception.AdminNotFoundException;
import com.softjourn.balance.transfer.client.exception.ChaincodeNotFoundException;
import com.softjourn.balance.transfer.client.exception.OrganizationNotFoundException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Data
@Component
@ConfigurationProperties("network")
public class ChainNetworkImpl implements ChainNetwork {

    private Orderer orderer;

    private List<Organization> organizations;

    private Channel channel;

    private Chaincode chaincode;

    private String endorsmentPolicy;

    @Override
    public org.hyperledger.fabric.sdk.User getAdminByOrganization(String org) {
        Optional<Organization> organization = getOrganization(org);
        if (organization.isPresent()) {
            Optional<User> optionalAdmin = organization.get().getUsers().stream().filter(User::isAdmin).findFirst();
            if (optionalAdmin.isPresent()) {
                User admin = optionalAdmin.get();
                return new ChainUser(admin.getName(), new File(admin.getCertificate()), new File(admin.getPrivateKey()), admin.getMsp());
            } else {
                throw new AdminNotFoundException(format("Admin was not set for organization: %s", org));
            }
        } else {
            throw new OrganizationNotFoundException(format("Such organization as %s was not found in property file", org));
        }
    }

    @Override
    public List<Peer> getPeers(String org) {
        Optional<Organization> organization = getOrganization(org);
        if (organization.isPresent()) {
            return organization.get().getPeers();
        } else {
            throw new OrganizationNotFoundException(format("Such organization as %s was not found in property file", org));
        }
    }

    private Optional<Organization> getOrganization(String org) {
        return this.organizations.stream()
                .filter(o -> o.getName().equals(org)).findAny();
    }

    public Chaincode getChaincode(String name) {
        if (this.chaincode.getName().equals(name)) {
            return this.chaincode;
        } else {
            throw new ChaincodeNotFoundException(format("Chaincode with channelName %s was not found in property file", name));
        }
    }

}
