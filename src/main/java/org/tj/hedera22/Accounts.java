package org.tj.hedera22;

import com.hedera.hashgraph.sdk.AccountId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Accounts {
    public static final AccountId OPERATOR_ID = AccountId.fromString("0.0.34260914");
    public static final AccountId PARTICIPANT_1 = AccountId.fromString("0.0.34260933");
    public static final AccountId PARTICIPANT_2 = AccountId.fromString("0.0.34260934");

    public static final Map<AccountId, String> whoIs = new ConcurrentHashMap<>();

    static {
        whoIs.put(OPERATOR_ID, "Operator");
        whoIs.put(PARTICIPANT_1, "Participant 1");
        whoIs.put(PARTICIPANT_2, "Participant 2");
    }
}
