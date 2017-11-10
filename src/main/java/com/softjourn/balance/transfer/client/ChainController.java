package com.softjourn.balance.transfer.client;

import com.softjourn.balance.transfer.client.dto.CreateChannelDTO;
import com.softjourn.balance.transfer.client.dto.InitializeChaincodeDTO;
import com.softjourn.balance.transfer.client.dto.InstallChaincodeDTO;
import com.softjourn.balance.transfer.client.dto.RequestChaincodeDTO;
import com.softjourn.balance.transfer.client.dto.TransactionInfoDTO;
import com.softjourn.balance.transfer.client.services.ChainService;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
@RequestMapping(value = "")
public class ChainController {

    private final ChainService chainService;

    @Autowired
    public ChainController(ChainService chainService) {
        this.chainService = chainService;
    }

    @PostMapping(value = "/channel")
    @ResponseBody
    public Map<String, Object> createChannel(@RequestBody CreateChannelDTO channelDTO) throws InvalidArgumentException, TransactionException, ProposalException, IOException {
        Channel channel = this.chainService.checkAndCreateChannel(channelDTO.getChannelName(),
                channelDTO.getOrganization(),
                channelDTO.getPeers());
        channel.shutdown(true);
        return new HashMap<String, Object>() {{
            put("Status", "OK");
            put("Message", format("Channel with channelName %s was created successfully", channelDTO.getChannelName()));
        }};
    }

    @GetMapping(value = "/channel")
    @ResponseBody
    public Map<String, Object> channelInfo(@RequestParam("channelName") String name, @RequestParam("org") String org, @RequestParam("peers") String[] peers) throws InvalidArgumentException, TransactionException, ProposalException, IOException {
        List<BlockchainInfo> blockchainInfo = this.chainService.getBlockchainInfo(name, org, peers);
        return new HashMap<String, Object>() {{
            put("Status", "OK");
            put("Height", blockchainInfo.get(0).getHeight());
            put("CurrentBlockHash", blockchainInfo.get(0).getCurrentBlockHash());
            put("PreviousBlockHash", blockchainInfo.get(0).getPreviousBlockHash());
        }};
    }

    @PostMapping(value = "/chaincode")
    @ResponseBody
    public Map<String, Object> installChaincode(@RequestBody InstallChaincodeDTO chaincodeDTO) throws InvalidArgumentException, TransactionException, ProposalException, IOException {
        Collection<ProposalResponse> responses = this.chainService.installChaincode(chaincodeDTO.getChannelName(),
                chaincodeDTO.getChaincodeName(),
                chaincodeDTO.getOrganization(),
                chaincodeDTO.getPeers());
        return new HashMap<String, Object>() {{
            put("Status", "OK");
            put("Message", format("Chaincode with name %s was installed successfully", chaincodeDTO.getChaincodeName()));
        }};
    }

    @PostMapping(value = "/chaincode/initialize")
    @ResponseBody
    public Map<String, Object> initializeChiancode(@RequestBody InitializeChaincodeDTO chaincodeDTO) throws InvalidArgumentException, TransactionException, ProposalException, IOException, InterruptedException, ExecutionException, TimeoutException, ChaincodeEndorsementPolicyParseException {
        this.chainService.initializeChaincode(chaincodeDTO.getChannelName(),
                chaincodeDTO.getChaincodeName(),
                chaincodeDTO.getOrganization(),
                chaincodeDTO.getPeers(),
                chaincodeDTO.getArgs());
        return new HashMap<String, Object>() {{
            put("Status", "OK");
            put("Message", format("Chaincode with name %s was initialized successfully", chaincodeDTO.getChaincodeName()));
        }};
    }

    @PostMapping(value = "/chaincode/query")
    @ResponseBody
    public Map<String, Object> queryChiancode(@RequestBody RequestChaincodeDTO chaincodeDTO) throws InvalidArgumentException, TransactionException, ProposalException, IOException, InterruptedException, ExecutionException, TimeoutException, ChaincodeEndorsementPolicyParseException {
        String result = this.chainService.query(chaincodeDTO.getChannelName(),
                chaincodeDTO.getChaincodeName(),
                chaincodeDTO.getFunctionName(),
                chaincodeDTO.getOrganization(),
                chaincodeDTO.getPeers(),
                chaincodeDTO.getArgs());
        return new HashMap<String, Object>() {{
            put("Status", "OK");
            put("Result", result);
        }};
    }

    @PostMapping(value = "/chaincode/transaction")
    @ResponseBody
    public Map<String, Object> transactionChiancode(@RequestBody RequestChaincodeDTO chaincodeDTO) throws InvalidArgumentException, TransactionException, ProposalException, IOException, InterruptedException, ExecutionException, TimeoutException, ChaincodeEndorsementPolicyParseException {
        String result = this.chainService.move(chaincodeDTO.getChannelName(),
                chaincodeDTO.getChaincodeName(),
                chaincodeDTO.getFunctionName(),
                chaincodeDTO.getOrganization(),
                chaincodeDTO.getPeers(),
                chaincodeDTO.getArgs());
        return new HashMap<String, Object>() {{
            put("Status", "OK");
            put("TransactionID", result);
        }};
    }

}
