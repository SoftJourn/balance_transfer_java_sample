package com.softjourn.balance.transfer.client.services;

import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface ChainService {

    Channel createChannel(String name, String org) throws IOException, InvalidArgumentException, TransactionException;

    Channel createChannelWithPeers(String name, String org, String peers[]) throws IOException, InvalidArgumentException, TransactionException, ProposalException;

    Channel checkAndCreateChannel(String name, String org, String[] peers) throws IOException, InvalidArgumentException, TransactionException, ProposalException;

    List<BlockchainInfo> getBlockchainInfo(String name, String org, String[] peers) throws TransactionException, InvalidArgumentException, ProposalException;

    Channel getChannel(String name, String org, String[] peers) throws InvalidArgumentException, TransactionException;

    Collection<ProposalResponse> installChaincode(String channelName, String chaincodeName, String org, String[] peers) throws TransactionException, InvalidArgumentException, ProposalException;

    void initializeChaincode(String channelName, String chaincodeName, String org, String[] peers, String[] args) throws TransactionException, InvalidArgumentException, ProposalException, IOException, ChaincodeEndorsementPolicyParseException, InterruptedException, ExecutionException, TimeoutException;

    String query(String channelName, String chaincodeName, String functionName, String org, String[] peers, String[] args) throws TransactionException, InvalidArgumentException, ProposalException, IOException, ChaincodeEndorsementPolicyParseException, InterruptedException, ExecutionException, TimeoutException;

    String move(String channelName, String chaincodeName, String functionName, String org, String[] peers, String[] args) throws TransactionException, InvalidArgumentException, ProposalException, IOException, ChaincodeEndorsementPolicyParseException, InterruptedException, ExecutionException, TimeoutException;
}
