package org.tj.hedera22;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class DeployDLOBEx {
    private static Logger logger = LoggerFactory.getLogger(DeployDLOBEx.class);

    private DeployDLOBEx() {
    }

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, IOException, ReceiptStatusException {
        long t0 = System.currentTimeMillis();

        final ClassLoader cl = DeployDLOBEx.class.getClassLoader();
        final Gson gson = new Gson();
        JsonObject jsonObject;

        try (InputStream jsonStream = cl.getResourceAsStream("DLOBEX.json")) {
            if (jsonStream == null) {
                throw new RuntimeException("Failed to get DLOBEX.json");
            }
            jsonObject = gson.fromJson(new InputStreamReader(jsonStream, StandardCharsets.UTF_8), JsonObject.class);
        }

        final String byteCodeHex = jsonObject.getAsJsonPrimitive("bytecode").getAsString();
        final byte[] byteCode = byteCodeHex.getBytes(StandardCharsets.UTF_8);

        final Client client = HederaClient.CLIENT_TESTNET;

        client.setOperator(Accounts.OPERATOR_ID, Accounts.OPERATOR_KEY);

        client.setDefaultMaxTransactionFee(new Hbar(1000000));
        client.setDefaultMaxQueryPayment(new Hbar(100000));

        final TransactionResponse fileTransactionResponse = new FileCreateTransaction()
                .setKeys(Accounts.OPERATOR_KEY)
                .setContents("")
                .execute(client);

        final TransactionReceipt fileReceipt = fileTransactionResponse.getReceipt(client);
        final FileAppendTransaction fat = new FileAppendTransaction()
                .setFileId(fileReceipt.fileId)
                .setMaxChunks(40);
        fat.setContents(byteCode);
        fat.execute(client);

        final FileId newFileId = Objects.requireNonNull(fileReceipt.fileId);

        long tf = (System.currentTimeMillis() - t0) / 1000;
        logger.info(tf + " secs. Contract bytecode file: " + newFileId);

        final TransactionResponse contractTransactionResponse = new ContractCreateTransaction()
                .setBytecodeFileId(newFileId)
                .setGas(4000000)
                .setConstructorParameters(
                        new ContractFunctionParameters()
                                .addAddress(Contracts.TOKEN_HBAR_ACCOUNT.toSolidityAddress())
                                .addAddress(Contracts.TOKEN_USD_ACCOUNT.toSolidityAddress())
                )
                .execute(client);

        try {
            final TransactionReceipt contractReceipt = contractTransactionResponse.getReceipt(client);
            final ContractId newContractId = Objects.requireNonNull(contractReceipt.contractId);
            logger.info("DLOBEX contract ID: " + newContractId);
            logger.info("DLOBEX Solidity address: " + newContractId.toSolidityAddress());
        } catch (ReceiptStatusException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }
}