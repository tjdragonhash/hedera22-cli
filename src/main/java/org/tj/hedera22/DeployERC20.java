package org.tj.hedera22;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class DeployERC20 {
    private static Logger logger = LoggerFactory.getLogger(DeployERC20.class);

    private DeployERC20() {
    }

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, IOException, ReceiptStatusException {
        long t0 = System.currentTimeMillis();

        final ClassLoader cl = DeployERC20.class.getClassLoader();
        final Gson gson = new Gson();
        JsonObject jsonObject;

        try (InputStream jsonStream = cl.getResourceAsStream("Gen20Token.json")) {
            if (jsonStream == null) {
                throw new RuntimeException("Failed to get Gen20Token.json");
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
                                .addUint256(BigInteger.valueOf(1_000_000))
                                .addString("HBAR")
                                .addString("HBAR")
                )
                .execute(client);

        try {
            final TransactionReceipt contractReceipt = contractTransactionResponse.getReceipt(client);
            final ContractId newContractId = Objects.requireNonNull(contractReceipt.contractId);
            logger.info("Gen20Token contract ID: " + newContractId);
            logger.info("Gen20Token Solidity address: " + newContractId.toSolidityAddress());
        } catch (ReceiptStatusException e) {
            logger.error(e.getMessage(), e);
        }
    }
}