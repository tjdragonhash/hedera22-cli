package org.tj.hedera22;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PrivateKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Accounts {
    public static final AccountId OPERATOR_ID = AccountId.fromString("0.0.xxxx");
    public static final PrivateKey OPERATOR_KEY = PrivateKey.fromString("xxxx");

    public static final AccountId PARTICIPANT_1 = AccountId.fromString("0.0.xxxx");
    public static final PrivateKey PARTICIPANT_1_KEY = PrivateKey.fromString("xxxx");

    public static final AccountId PARTICIPANT_2 = AccountId.fromString("0.0.xxxx");
    public static final PrivateKey PARTICIPANT_2_KEY = PrivateKey.fromString("xxxx");

    public static final Map<AccountId, String> whoIs = new ConcurrentHashMap<>();

    static {
        whoIs.put(OPERATOR_ID, "Operator");
        whoIs.put(PARTICIPANT_1, "Participant 1");
        whoIs.put(PARTICIPANT_2, "Participant 2");
    }
}
