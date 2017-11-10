package com.softjourn.balance.transfer.client.services;

import com.softjourn.balance.transfer.client.exception.ChannelAlreadyExistsException;
import com.softjourn.balance.transfer.client.exception.ChannelNotFoundException;
import com.softjourn.balance.transfer.client.exception.InvalidTransactionProposalException;
import com.softjourn.balance.transfer.client.properties.ChainNetworkImpl;
import com.softjourn.balance.transfer.client.properties.Chaincode;
import com.softjourn.balance.transfer.client.properties.Channel;
import com.softjourn.balance.transfer.client.properties.Orderer;
import com.softjourn.balance.transfer.client.properties.Peer;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

@Service
public class ChainServiceImpl implements ChainService {

    private final ChainNetworkImpl network;

    private final HFClient client;

    @Autowired
    public ChainServiceImpl(ChainNetworkImpl network)
            throws CryptoException, InvalidArgumentException {
        this.network = network;
        this.client = HFClient.createNewInstance();
        CryptoSuite cs = CryptoSuite.Factory.getCryptoSuite();
        client.setCryptoSuite(cs);
    }

    @Override
    public org.hyperledger.fabric.sdk.Channel createChannel(String name, String org)
            throws IOException, InvalidArgumentException, TransactionException {
        User admin = this.network.getAdminByOrganization(org);
        Channel channel = getChannelProperties(name);
        Orderer orderer = this.network.getOrderer();

        ChannelConfiguration configuration = new ChannelConfiguration(new File(network.getChannel().getConfigFile()));
        byte[] channelConfigurationSignature = client.getChannelConfigurationSignature(configuration, admin);
        return client.newChannel(channel.getName(),
                client.newOrderer(orderer.getName(), orderer.getUrl(), prepareProperties(orderer.getCertificate())),
                configuration,
                channelConfigurationSignature);
    }

    @Override
    public org.hyperledger.fabric.sdk.Channel createChannelWithPeers(String name, String org, String[] peers)
            throws IOException, InvalidArgumentException, TransactionException, ProposalException {
        org.hyperledger.fabric.sdk.Channel channel = createChannel(name, org);
        for (Peer peer : getPeers(org, peers)) {
            channel.joinPeer(this.client.newPeer(peer.getName(), peer.getUrl(), prepareProperties(peer.getCertificate())));
        }
        channel.initialize();
        return channel;
    }

    @Override
    public org.hyperledger.fabric.sdk.Channel checkAndCreateChannel(String name, String org, String[] peers)
            throws IOException, InvalidArgumentException, TransactionException, ProposalException {
        boolean checkChannel = false;
        User admin = this.network.getAdminByOrganization(org);
        client.setUserContext(admin);
        for (Peer peer : getPeers(org, peers)) {
            Set<String> channels = this.client.queryChannels(this.client.newPeer(peer.getName(), peer.getUrl(),
                    prepareProperties(peer.getCertificate())));
            if (channels.isEmpty()) {
                return createChannelWithPeers(name, org, peers);
            } else {
                for (String channel : channels) {
                    if (channel.equals(name)) {
                        checkChannel = true;
                    }
                }
            }
        }
        if (checkChannel) {
            throw new ChannelAlreadyExistsException(format("Channel with channelName %s and peers %s already exists",
                    name, join(" ", peers)));
        } else {
            return createChannelWithPeers(name, org, peers);
        }
    }

    @Override
    public List<BlockchainInfo> getBlockchainInfo(String name, String org, String[] peers)
            throws TransactionException, InvalidArgumentException, ProposalException {
        List<BlockchainInfo> infoList = new ArrayList<>();
        org.hyperledger.fabric.sdk.Channel channel = getChannel(name, org, peers);
        for (org.hyperledger.fabric.sdk.Peer peer : channel.getPeers()) {
            infoList.add(channel.queryBlockchainInfo(peer));
        }
        channel.shutdown(true);
        return infoList;
    }

    @Override
    public org.hyperledger.fabric.sdk.Channel getChannel(String name, String org, String[] peers)
            throws InvalidArgumentException, TransactionException {
        User admin = this.network.getAdminByOrganization(org);
        Orderer orderer = this.network.getOrderer();
        client.setUserContext(admin);
        org.hyperledger.fabric.sdk.Channel channel = client.newChannel(name);
        for (Peer peer : getPeers(org, peers)) {
            channel.addPeer(this.client.newPeer(peer.getName(), peer.getUrl(), prepareProperties(peer.getCertificate())));
        }
        channel.addOrderer(client.newOrderer(orderer.getName(), orderer.getUrl(), prepareProperties(orderer.getCertificate())));
        channel.initialize();
        return channel;
    }

    @Override
    public Collection<ProposalResponse> installChaincode(String channelName, String chaincodeName, String org, String[] peers)
            throws TransactionException, InvalidArgumentException, ProposalException {
        Chaincode chaincode = this.network.getChaincode(chaincodeName);
        org.hyperledger.fabric.sdk.Channel channel = getChannel(channelName, org, peers);

        final ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincode.getName())
                .setVersion(chaincode.getVersion())
                .setPath(chaincode.getPathToFile()).build();

        InstallProposalRequest installProposalRequest = this.client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeID);
        installProposalRequest.setChaincodeSourceLocation(new File(chaincode.getSourceLocation()));
        installProposalRequest.setChaincodeVersion(chaincode.getVersion());
        ArrayList<ProposalResponse> responses = new ArrayList<>(client.sendInstallProposal(installProposalRequest, channel.getPeers()));
        channel.shutdown(true);
        return responses;
    }

    @Override
    public void initializeChaincode(String channelName, String chaincodeName, String org, String[] peers, String[] args)
            throws TransactionException, InvalidArgumentException, ProposalException, InterruptedException, ExecutionException {
        Chaincode chaincode = this.network.getChaincode(chaincodeName);
        org.hyperledger.fabric.sdk.Channel channel = getChannel(channelName, org, peers);

        final ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincode.getName())
                .setVersion(chaincode.getVersion())
                .setPath(chaincode.getPathToFile()).build();

        InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
        instantiateProposalRequest.setChaincodeID(chaincodeID);
        instantiateProposalRequest.setChaincodeVersion(chaincode.getVersion());
        instantiateProposalRequest.setFcn("init");
        instantiateProposalRequest.setArgs(args);
        instantiateProposalRequest.setTransientMap(prepareInstantiateProposal());

        ArrayList<ProposalResponse> proposalResponses = new ArrayList<>(channel.sendInstantiationProposal(instantiateProposalRequest, channel.getPeers()));

        CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture =
                channel.sendTransaction(proposalResponses, channel.getOrderers());

        try {
            transactionEventCompletableFuture.get(5l, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
        }
        channel.shutdown(true);
    }

    @Override
    public String query(String channelName, String chaincodeName, String functionName, String org, String[] peers, String[] args)
            throws TransactionException, InvalidArgumentException, ProposalException {
        Chaincode chaincode = this.network.getChaincode(chaincodeName);
        org.hyperledger.fabric.sdk.Channel channel = getChannel(channelName, org, peers);

        final ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincode.getName())
                .setVersion(chaincode.getVersion())
                .setPath(chaincode.getPathToFile()).build();

        QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
        queryByChaincodeRequest.setArgs(args);
        queryByChaincodeRequest.setFcn(functionName);
        queryByChaincodeRequest.setChaincodeID(chaincodeID);
        queryByChaincodeRequest.setTransientMap(prepareQueryByChaincode());

        List<ProposalResponse> proposalResponses = new ArrayList<>(channel.queryByChaincode(queryByChaincodeRequest, channel.getPeers()));
        channel.shutdown(true);
        return new String(proposalResponses.get(0).getChaincodeActionResponsePayload());
    }

    @Override
    public String move(String channelName, String chaincodeName, String functionName, String org, String[] peers, String[] args)
            throws TransactionException, InvalidArgumentException, ProposalException, ExecutionException, InterruptedException {
        Chaincode chaincode = this.network.getChaincode(chaincodeName);
        org.hyperledger.fabric.sdk.Channel channel = getChannel(channelName, org, peers);

        final ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincode.getName())
                .setVersion(chaincode.getVersion())
                .setPath(chaincode.getPathToFile()).build();

        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setFcn("move");
        transactionProposalRequest.setArgs(args);
        transactionProposalRequest.setTransientMap(prepareTransactionProposal());

        List<ProposalResponse> proposalResponses = new ArrayList<>(channel.sendTransactionProposal(transactionProposalRequest));

        boolean successful = true;
        for (ProposalResponse response : proposalResponses) {
            if (response.isInvalid()) {
                successful = false;
            }
        }
        if (successful) {
            CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture =
                    channel.sendTransaction(proposalResponses, channel.getOrderers());

            try {
                transactionEventCompletableFuture.get(1l, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
            }
            channel.shutdown(true);
        } else {
            channel.shutdown(true);
            throw new InvalidTransactionProposalException("Transaction proposal is invalid");
        }
        return proposalResponses.get(0).getTransactionID();
    }

    private Channel getChannelProperties(String name) {
        Channel channel = this.network.getChannel();
        if (channel.getName().equals(name)) {
            return channel;
        } else {
            throw new ChannelNotFoundException(format("Channel with channelName: %s was not found or bad initialized", name));
        }
    }

    private static Properties prepareProperties(String certificate) {
        File cert = Paths.get(certificate).toFile();
        Properties ret = new Properties();
        ret.setProperty("pemFile", cert.getAbsolutePath());
        ret.setProperty("sslProvider", "openSSL");
        ret.setProperty("trustServerCertificate", "true");
        ret.setProperty("negotiationType", "TLS");
        ret.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
        ret.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit.SECONDS});
        ret.put("grpc.NettyChannelBuilderOption.keepAliveWithoutCalls", new Object[]{true});
        return ret;
    }

    private static Map<String, byte[]> prepareInstantiateProposal() {
        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        return tm;
    }

    private static Map<String, byte[]> prepareQueryByChaincode() {
        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        tm.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
        return tm;
    }

    private static Map<String, byte[]> prepareTransactionProposal() {
        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8)); //Just some extra junk in transient map
        tm.put("method", "TransactionProposalRequest".getBytes(UTF_8)); // ditto
        return tm;
    }

    private List<Peer> getPeers(String org, String[] peers) {
        return this.network.getPeers(org).stream()
                .filter(peer -> Arrays.stream(peers).anyMatch(peer.getName()::equals))
                .collect(toList());
    }
}
