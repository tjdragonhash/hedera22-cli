package org.tj.hedera22;

import com.hedera.hashgraph.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class CreateAccount {
    private static Logger logger = LoggerFactory.getLogger(CreateAccount.class);

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        logger.info("CreateAccount");

        // Create HSM an Ed25519 key pair - Here we simulate via software
        final PrivateKey customerPrivateKey = PrivateKey.generate();
        final PublicKey customerPublicKey = customerPrivateKey.getPublicKey();
        logger.info("customerPublicKey: " + Base64.getEncoder().encodeToString(customerPublicKey.toBytes()));
        logger.info("customerPrivateKey: " + customerPrivateKey);

        final AccountId operatorId = Accounts.OPERATOR_ID;
        final PrivateKey operatorPrivateKey = Accounts.OPERATOR_KEY;

        final Client client = HederaClient.CLIENT_TESTNET;
        client.setOperator(operatorId, operatorPrivateKey);

        final TransactionResponse transactionResponse = new AccountCreateTransaction()
                .setReceiverSignatureRequired(false) // Must be true for FATF-16
                .setKey(customerPublicKey)
                .freezeWith(client)
                .signWith(operatorPrivateKey.getPublicKey(), signWithHsm(operatorPrivateKey))
                .execute(client);

        final TransactionReceipt transactionReceipt = transactionResponse.getReceipt(client);
        logger.info(transactionReceipt.status.toString() + " - New account id " + transactionReceipt.accountId);
    }

    private static Function<byte[],byte[]> signWithHsm(final PrivateKey operatorPrivateKey) {
        return (
                operatorPrivateKey::sign
        );
    }
}
