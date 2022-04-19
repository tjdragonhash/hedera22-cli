package org.tj.hedera22;

import com.hedera.hashgraph.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.TimeoutException;

public class TransferTokens {
    private static Logger logger = LoggerFactory.getLogger(TransferTokens.class);

    private static final Client client = HederaClient.CLIENT_TESTNET;

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        client.setOperator(Accounts.OPERATOR_ID, PrivateKeys.OPERATOR_KEY);

        transfer(Accounts.PARTICIPANT_1.toSolidityAddress(), Contracts.TOKEN_HBAR_ACCOUNT);
        transfer(Accounts.PARTICIPANT_1.toSolidityAddress(), Contracts.TOKEN_USD_ACCOUNT);

        transfer(Accounts.PARTICIPANT_2.toSolidityAddress(), Contracts.TOKEN_HBAR_ACCOUNT);
        transfer(Accounts.PARTICIPANT_2.toSolidityAddress(), Contracts.TOKEN_USD_ACCOUNT);
    }

    private static void transfer(final String to, final ContractId contractId) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        final TransactionResponse contractExecTransactionResponse = new ContractExecuteTransaction()
                .setContractId(contractId)
                .setGas(1_000_000)
                .setFunction("transfer", new ContractFunctionParameters()
                        .addAddress(to)
                        .addUint256(BigInteger.valueOf(100_000)))
                .execute(client);

        final TransactionReceipt receipt = contractExecTransactionResponse.getReceipt(client);
        logger.info(receipt.toString());

        final ContractFunctionResult contractUpdateResult = new ContractCallQuery()
                .setContractId(contractId)
                .setGas(100_000) // gasUsed=2876
                .setQueryPayment(new Hbar(1))
                .setFunction("balanceOf", new ContractFunctionParameters()
                        .addAddress(to))
                .execute(client);
        final BigInteger balance = contractUpdateResult.getUint256(0);
        logger.info(balance.toString());
    }
}
