package org.tj.hedera22;

import com.hedera.hashgraph.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.TimeoutException;

public class ApproveTransfer {
    private static Logger logger = LoggerFactory.getLogger(ApproveTransfer.class);

    private static final Client client = HederaClient.CLIENT_TESTNET;

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        final String spender = Contracts.DLOBEX_ACC_ID.toSolidityAddress();

        client.setOperator(Accounts.PARTICIPANT_1, PrivateKeys.PARTICIPANT_1_KEY);
        approve(spender, Contracts.TOKEN_HBAR_ACCOUNT);
        approve(spender, Contracts.TOKEN_USD_ACCOUNT);

        client.setOperator(Accounts.PARTICIPANT_2, PrivateKeys.PARTICIPANT_2_KEY);
        approve(spender, Contracts.TOKEN_HBAR_ACCOUNT);
        approve(spender, Contracts.TOKEN_USD_ACCOUNT);
    }

    private static void approve(final String spender, final ContractId contractId) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        logger.info("*** APPROVE ***");
        final TransactionResponse contractExecTransactionResponse = new ContractExecuteTransaction()
                .setContractId(contractId)
                .setGas(1_000_000)
                .setFunction("approve", new ContractFunctionParameters()
                        .addAddress(spender)
                        .addUint256(BigInteger.valueOf(2_000_000)))
                .execute(client);

        final TransactionReceipt receipt = contractExecTransactionResponse.getReceipt(client);
        System.out.println(receipt);
    }
}
