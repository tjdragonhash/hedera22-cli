package org.tj.hedera22;

import com.hedera.hashgraph.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public final class TransferHBARs {
    private static Logger logger = LoggerFactory.getLogger(TransferHBARs.class);

    private TransferHBARs() {
    }

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        final Client client = HederaClient.CLIENT_TESTNET;
        client.setOperator(Accounts.OPERATOR_ID, PrivateKeys.OPERATOR_KEY);

        final List<AccountId> beneficiaries = Arrays.asList(Accounts.PARTICIPANT_1, Accounts.PARTICIPANT_2);
        final Hbar amount = new Hbar(200);

        for(AccountId beneficiary : beneficiaries) {
            final Hbar senderBalanceBefore = new AccountBalanceQuery()
                    .setAccountId(Accounts.OPERATOR_ID)
                    .execute(client)
                    .hbars;

            final Hbar receiptBalanceBefore = new AccountBalanceQuery()
                    .setAccountId(beneficiary)
                    .execute(client)
                    .hbars;

            logger.info("OPERATOR_ID balance = " + senderBalanceBefore);
            logger.info("RecipientId balance = " + receiptBalanceBefore);

            final TransactionResponse transactionResponse = new TransferTransaction()
                    .addHbarTransfer(Accounts.OPERATOR_ID, amount.negated())
                    .addHbarTransfer(beneficiary, amount)
                    .setTransactionMemo("Native HBAR Transfer")
                    .execute(client);

            logger.info("transaction ID: " + transactionResponse);

            final TransactionRecord record = transactionResponse.getRecord(client);

            logger.info("transferred " + amount + "...");

            final Hbar senderBalanceAfter = new AccountBalanceQuery()
                    .setAccountId(Accounts.OPERATOR_ID)
                    .execute(client)
                    .hbars;

            final Hbar receiptBalanceAfter = new AccountBalanceQuery()
                    .setAccountId(beneficiary)
                    .execute(client)
                    .hbars;

            logger.info("OPERATOR_ID balance = " + senderBalanceAfter);
            logger.info("RecipientId balance = " + receiptBalanceAfter);
            logger.info("Transfer memo: " + record.transactionMemo);
        }
    }
}