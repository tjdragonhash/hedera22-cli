package org.tj.hedera22;

import com.hedera.hashgraph.sdk.*;

import java.util.concurrent.TimeoutException;

public final class GetHederaAccountBalance {
    private static final Client client = HederaClient.CLIENT_TESTNET;

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException {
        System.out.println("OPERATOR_ID balance = " + getBalance(Accounts.OPERATOR_ID));
    }

    private static Hbar getBalance(final AccountId accountId) throws PrecheckStatusException, TimeoutException {
        return new AccountBalanceQuery()
                .setAccountId(accountId)
                .execute(client)
                .hbars;
    }
}