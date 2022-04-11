package org.tj.hedera22;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;

import java.util.HashMap;
import java.util.Map;

public final class HederaClient {
    public static Client CLIENT_TESTNET = Client.forName("testnet");
    public static Client CLIENT_LOCAL;

    static {
        final Map<String, AccountId> network = new HashMap<>();
        network.put("0.testnet.hedera.com:50211", new AccountId(3));
        network.put("1.testnet.hedera.com:50211", new AccountId(4));
        network.put("2.testnet.hedera.com:50211", new AccountId(5));
        network.put("3.testnet.hedera.com:50211", new AccountId(6));
        network.put("4.testnet.hedera.com:50211", new AccountId(7));
        network.put("5.testnet.hedera.com:50211", new AccountId(8));
        network.put("6.testnet.hedera.com:50211", new AccountId(9));

        CLIENT_LOCAL = Client.forNetwork(network);
    }
}
