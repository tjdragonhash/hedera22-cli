package org.tj.hedera22;

import com.hedera.hashgraph.sdk.*;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class CLI {
    private static Logger logger = LoggerFactory.getLogger(CLI.class);
    private final Client client;
    private final long defaultGas = 4000000;
    private final double FX = 0.221;

    private AccountId currentAccount = Accounts.OPERATOR_ID;
    private PrivateKey currentPrivateKey = Accounts.OPERATOR_KEY;

    public CLI() {
        logger.info("Hedera CLI");
        client = HederaClient.CLIENT_TESTNET;
        client.setOperator(currentAccount, currentPrivateKey);
        menu();
    }

    private void menu() {
        logger.info("Operator: " + Accounts.OPERATOR_ID);
        logger.info("Participants 1: " + Accounts.PARTICIPANT_1);
        logger.info("Participants 2: " + Accounts.PARTICIPANT_2);

        while(true) {
            client.setOperator(currentAccount, currentPrivateKey);
            try {
                logger.info("");
                logger.info("Hedera Menu. Please select an option:");
                logger.info(" Acting account: " + this.currentAccount + " (" + Accounts.whoIs.get(currentAccount) + ")");
                logger.info("   1. Exit");
                logger.info("   2. Allow Trading (~ 1ℏ)");
                logger.info("   3. Stop Trading (~ 1ℏ)");
                logger.info("   4. Add All Participants (~ 2ℏ)");
                logger.info("   5. Select participant (free)");
                logger.info("   6. Display order book (~ 7ℏ)");
                logger.info("   7. Place Limit Order (~ 1ℏ)");
                logger.info("   8. Place Market Order (~ 1ℏ)");
                logger.info("   9. Display Settlement Instructions (~ 1ℏ)");
                logger.info("   10. Display balances (~ 8ℏ)");
                logger.info("   11. Display trading allowed status (~ 0.1ℏ)");
                logger.info("   12. Display latest debug (~ 0ℏ)");
                logger.info("   13. Reset");

                final Scanner in = new Scanner(System.in);
                final int option = in.nextInt();
                final AccountId actingAccount = currentAccount;
                final Hbar b1 = getBalance(actingAccount);
                processOption(option);
                final Hbar b2 = getBalance(actingAccount);
                final BigDecimal used = b1.getValue().subtract(b2.getValue());
                logger.info("(HBAR Used: " +used+" ~ "+used.doubleValue()*FX+" USD) ");
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } // while
    }

    private void processOption(final int option) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        switch (option) {
            case 1:
                logger.info("Good bye");
                System.exit(0);
                break;
            case 2:
                allowTrading("start_trading");
                break;
            case 3:
                allowTrading("stop_trading");
                break;
            case 4:
                addAllParticipants();
                break;
            case 5:
                selectParticipant();
                break;
            case 6:
                displayOrderBook();
                break;
            case 7:
                placeLimitOrder();
                break;
            case 8:
                placeMarketOrder();
                break;
            case 9:
                displaySettlement();
                break;
            case 10:
                displayBalances();
                break;
            case 11:
                displayTradingAllowedStatus();
                break;
            case 12:
                debug();
                break;
            case 13:
                reset();
                break;
            default:
                logger.warn("Unknown option.");
        }
    }

    private void displaySettlement() throws PrecheckStatusException, TimeoutException {
        logger.info("Displaying Settlements");
        final long numberOfSettlements = numberOfSettlements().longValue();

        for(long index = 0L; index < numberOfSettlements; index++) {
            final Settlement settlement = getSettlement(BigInteger.valueOf(index));
            logger.info(settlement.toString());
        }
    }

    private BigInteger numberOfSettlements() throws PrecheckStatusException, TimeoutException {
        final ContractFunctionResult contractUpdateResult = new ContractCallQuery()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas) // gasUsed=2876
                .setQueryPayment(new Hbar(1))
                .setFunction("get_number_of_settlements")
                .execute(client);
        return contractUpdateResult.getUint256(0);
    }

    private Settlement getSettlement(final BigInteger index) throws PrecheckStatusException, TimeoutException {
        final ContractFunctionResult contractUpdateResult = new ContractCallQuery()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas) // gasUsed=2876
                .setQueryPayment(new Hbar(1))
                .setFunction("get_settlement", new ContractFunctionParameters()
                        .addUint256(index))
                .execute(client);
        final String raw = Hex.toHexString(contractUpdateResult.asBytes());
        return DecodeHelper.toSettlement(raw);
    }

    private void reset() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        logger.info("Resetting...");
        final TransactionResponse contractExecTransactionResponse = new ContractExecuteTransaction()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas)
                .setFunction("reset")
                .execute(client);

        final TransactionReceipt transactionReceipt = contractExecTransactionResponse.getReceipt(client);
        logger.info(transactionReceipt.status + ": " + transactionReceipt);
    }

    private void displayTradingAllowedStatus() throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        final boolean p1Allowed = isParticipantAllowed(Accounts.PARTICIPANT_1.toSolidityAddress());
        final boolean p2Allowed = isParticipantAllowed(Accounts.PARTICIPANT_2.toSolidityAddress());
        final boolean isTradingAllowed = isTradingAllowed();
        logger.info("TRADING allowed? " + isTradingAllowed);
        logger.info("PARTICIPANT_1 and PARTICIPANT_2 allowed to trade? " + p1Allowed + ", " + p2Allowed);
    }

    private void addAllParticipants() throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        addParticipant(Accounts.PARTICIPANT_1.toSolidityAddress());
        addParticipant(Accounts.PARTICIPANT_2.toSolidityAddress());
    }

    private TransactionReceipt addParticipant(final String participant) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        final TransactionResponse contractExecTransactionResponse = new ContractExecuteTransaction()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas)
                .setFunction("add_participant", new ContractFunctionParameters()
                        .addAddress(participant))
                .execute(client);

        final TransactionReceipt transactionReceipt = contractExecTransactionResponse.getReceipt(client);
        logger.info(transactionReceipt.status + ": " + transactionReceipt);
        return transactionReceipt;
    }

    private void debug() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        final ContractFunctionResult result = new ContractCallQuery()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas)
                .setQueryPayment(new Hbar(1))
                .setFunction("debug")
                .execute(client);
        final String debug = result.getString(0);
        logger.info("Latest Debug Statement: '" + debug + "'");
    }

    private boolean isParticipantAllowed(final String participant) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        final ContractFunctionResult result = new ContractCallQuery()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas)
                .setFunction("is_participant_allowed", new ContractFunctionParameters()
                        .addAddress(participant))
                .execute(client);
        final boolean allowed = result.getBool(0);
        return allowed;
    }

    private boolean isTradingAllowed() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        final ContractFunctionResult result = new ContractCallQuery()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas)
                .setFunction("is_trading_allowed")
                .execute(client);
        final boolean allowed = result.getBool(0);
        return allowed;
    }

    private TransactionReceipt allowTrading(final String ops) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        final TransactionResponse contractExecTransactionResponse = new ContractExecuteTransaction()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas)
                .setFunction(ops)
                .execute(client);

        final TransactionReceipt transactionReceipt = contractExecTransactionResponse.getReceipt(client);
        logger.info(transactionReceipt.status + ": " + transactionReceipt);
        return transactionReceipt;
    }

    private void placeLimitOrder() throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        logger.info("Placing Limit Order...");
        logger.info("Enter <verb> <size> <price> (for ex: 'true 100 22' - buy 100 hbar/usd at 22");
        final Scanner in = new Scanner(System.in);
        final String cmd = in.nextLine();
        final String[] data = cmd.split("\\s+");
        final boolean is_buy = Boolean.parseBoolean(data[0]);
        final BigInteger size = new BigInteger(data[1]);
        final BigInteger price = new BigInteger(data[2]);

        final TransactionReceipt transactionReceipt = placeLimitOrder(is_buy, size, price);
        logger.info(transactionReceipt.status + ": " + transactionReceipt);
    }

    private TransactionReceipt placeLimitOrder(final boolean isBuy, final BigInteger size, final BigInteger price) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        final BigInteger oid = BigInteger.valueOf(System.currentTimeMillis());
        logger.info("Creating order id " + oid + " Buy: "+ isBuy +", Size " + size + " @ Price " + price);

        final TransactionResponse contractExecTransactionResponse = new ContractExecuteTransaction()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas)
                .setFunction("place_limit_order", new ContractFunctionParameters()
                        .addUint256(oid) // External order id
                        .addBool(isBuy) // Buy or Sell
                        .addUint256(size) // Amount
                        .addUint256(price)) // Price
                .execute(client);

        return contractExecTransactionResponse.getReceipt(client);
    }

    private void placeMarketOrder() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        logger.info("Placing Market Order...");
        logger.info("Enter <verb> <size> (for ex: 'true 100' - buy 100 hbar/usd)");
        final Scanner in = new Scanner(System.in);
        final String cmd = in.nextLine();
        final String[] data = cmd.split("\\s+");
        final boolean is_buy = Boolean.parseBoolean(data[0]);
        final BigInteger size = new BigInteger(data[1]);

        final TransactionReceipt transactionReceipt = placeMarketOrder(is_buy, size);
        logger.info(transactionReceipt.status + ": " + transactionReceipt);
    }

    private TransactionReceipt placeMarketOrder(final boolean isBuy, final BigInteger size) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        logger.info("Creating market order Buy: "+ isBuy +", Size " + size);

        final TransactionResponse contractExecTransactionResponse = new ContractExecuteTransaction()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas)
                .setFunction("place_market_order", new ContractFunctionParameters()
                        .addBool(isBuy) // Buy or Sell
                        .addUint256(size)) // Amount
                .execute(client);

        return contractExecTransactionResponse.getReceipt(client);
    }

    private void displayBalances() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        final BigInteger hhbarBalance1 = balance(Accounts.PARTICIPANT_1.toSolidityAddress(), Contracts.TOKEN_HBAR_ACCOUNT);
        final BigInteger husdBalance1 = balance(Accounts.PARTICIPANT_1.toSolidityAddress(), Contracts.TOKEN_USD_ACCOUNT);

        final BigInteger hhbarBalance2 = balance(Accounts.PARTICIPANT_2.toSolidityAddress(), Contracts.TOKEN_HBAR_ACCOUNT);
        final BigInteger husdBalance2 = balance(Accounts.PARTICIPANT_2.toSolidityAddress(), Contracts.TOKEN_USD_ACCOUNT);

        final BigInteger hhbarAllowance1 = allowance(Accounts.PARTICIPANT_1.toSolidityAddress(), Contracts.DLOBEX_ACC_ID.toSolidityAddress(), Contracts.TOKEN_HBAR_ACCOUNT);
        final BigInteger husdAllowance1 = allowance(Accounts.PARTICIPANT_1.toSolidityAddress(), Contracts.DLOBEX_ACC_ID.toSolidityAddress(), Contracts.TOKEN_USD_ACCOUNT);

        final BigInteger hhbarAllowance2 = allowance(Accounts.PARTICIPANT_2.toSolidityAddress(), Contracts.DLOBEX_ACC_ID.toSolidityAddress(), Contracts.TOKEN_HBAR_ACCOUNT);
        final BigInteger husdAllowance2 = allowance(Accounts.PARTICIPANT_2.toSolidityAddress(), Contracts.DLOBEX_ACC_ID.toSolidityAddress(), Contracts.TOKEN_USD_ACCOUNT);

        logger.info("Displaying balances...");
        logger.info(" Operator: " + getBalance(Accounts.OPERATOR_ID));
        logger.info(" Participant 1: " + getBalance(Accounts.PARTICIPANT_1) +
                ", HHBAR Owned: " + hhbarBalance1 + ", HUSD Owned: " + husdBalance1 +
                ", HHBAR Allowance: " + hhbarAllowance1 +", HUSD Allowance: " + husdAllowance1);
        logger.info(" Participant 2: " + getBalance(Accounts.PARTICIPANT_2) +
                ", HHBAR Owned: " + hhbarBalance2 + ", HUSD Owned: " + husdBalance2 +
                ", HHBAR Allowance: " + hhbarAllowance2 +", HUSD Allowance: " + husdAllowance2);
    }

    private BigInteger balance(final String accountAddress, final ContractId contractId) throws PrecheckStatusException, TimeoutException {
        final ContractFunctionResult contractUpdateResult = new ContractCallQuery()
                .setContractId(contractId)
                .setGas(defaultGas) // gasUsed=2876
                .setQueryPayment(new Hbar(1))
                .setFunction("balanceOf", new ContractFunctionParameters()
                        .addAddress(accountAddress))
                .execute(client);
        return contractUpdateResult.getUint256(0);
    }

    private BigInteger allowance(final String accountAddress, final String spender, final ContractId contractId) throws PrecheckStatusException, TimeoutException {
        final ContractFunctionResult contractUpdateResult = new ContractCallQuery()
                .setContractId(contractId)
                .setGas(defaultGas) // gasUsed=2876
                .setQueryPayment(new Hbar(1))
                .setFunction("allowance", new ContractFunctionParameters()
                        .addAddress(accountAddress)
                        .addAddress(spender))
                .execute(client);
        return contractUpdateResult.getUint256(0);
    }

    private Hbar getBalance(final AccountId accountId) throws PrecheckStatusException, TimeoutException {
        return new AccountBalanceQuery()
                .setAccountId(accountId)
                .execute(client)
                .hbars;
    }

    private void displayOrderBook() throws PrecheckStatusException, TimeoutException {
        logger.info("Displaying order book...");

        final BigInteger[] buy_prices = prices("buy_prices");
        final List<Order> buyOrders = new LinkedList<>();
        for(BigInteger p : buy_prices) {
            final BigInteger[] buy_order_ids = orderIds("buy_order_ids", p);
            for(BigInteger oid : buy_order_ids) {
                final Order order = getOrder(oid);
                buyOrders.add(order);
            }
        }

        final BigInteger[] sell_prices = prices("sell_prices");
        final List<Order> sellOrders = new LinkedList<>();
        for(BigInteger p : sell_prices) {
            final BigInteger[] sell_order_ids = orderIds("sell_order_ids", p);
            for(BigInteger oid : sell_order_ids) {
                final Order order = getOrder(oid);
                sellOrders.add(order);
            }
        }

        Collections.reverse(buyOrders);

        logger.info("ORDER BOOK HBAR/HUSD");
        //           1649254700910 ccf 100 @ 22
        //
        logger.info("-- BUY -------------------- SELL --------------------");
        for(Order buyOrder : buyOrders) {
            logger.info(buyOrder.id + " " + owner(buyOrder) + " "  + buyOrder.size + " @ " + buyOrder.price);
        }
        for(Order sellOrder : sellOrders) {
            logger.info("                            " + sellOrder.size + " @ " + sellOrder.price + " " + owner(sellOrder) + " " + sellOrder.id);
        }
        logger.info("-----------------------------------------------------");
    }

    private BigInteger[] prices(final String ops) throws PrecheckStatusException, TimeoutException {
        final ContractFunctionResult contractUpdateResult = new ContractCallQuery()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas) // gasUsed=2876
                .setQueryPayment(new Hbar(1))
                .setFunction(ops)
                .execute(client);

        final String raw = Hex.toHexString(contractUpdateResult.asBytes());
        final BigInteger[] bigIntegers = DecodeHelper.toUint256Array(raw);
        return bigIntegers;
    }

    private BigInteger[] orderIds(final String ops, final BigInteger price) throws PrecheckStatusException, TimeoutException {
        final ContractFunctionResult contractUpdateResult = new ContractCallQuery()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas) // gasUsed=2876
                .setQueryPayment(new Hbar(1))
                .setFunction(ops, new ContractFunctionParameters()
                        .addUint256(price))
                .execute(client);
        final String raw = Hex.toHexString(contractUpdateResult.asBytes());
        final BigInteger[] bigIntegers = DecodeHelper.toUint256Array(raw);
        return bigIntegers;
    }

    private String owner(Order order) {
        return order.address.substring(order.address.length() -3);
    }

    private Order getOrder(final BigInteger orderId) throws PrecheckStatusException, TimeoutException {
        final ContractFunctionResult contractUpdateResult = new ContractCallQuery()
                .setContractId(Contracts.DLOBEX_ACC_ID)
                .setGas(defaultGas) // gasUsed=2876
                .setQueryPayment(new Hbar(1))
                .setFunction("get_order", new ContractFunctionParameters()
                        .addUint256(orderId))
                .execute(client);
        final String raw = Hex.toHexString(contractUpdateResult.asBytes());
        return DecodeHelper.toOrder(raw);
    }

    private void selectParticipant() {
        logger.info("Selecting participant for next operation...");
        logger.info(" 0 for operator, 1 for participant 1, 2 for participant 2");
        final Scanner in = new Scanner(System.in);
        final int option = in.nextInt();
        switch (option) {
            case 1:
                currentAccount = Accounts.PARTICIPANT_1;
                currentPrivateKey = Accounts.PARTICIPANT_1_KEY;
                break;
            case 2:
                currentAccount = Accounts.PARTICIPANT_2;
                currentPrivateKey = Accounts.PARTICIPANT_2_KEY;
                break;
            default:
                currentAccount = Accounts.OPERATOR_ID;
                currentPrivateKey = Accounts.OPERATOR_KEY;
        }
    }

    public static void main(String[] args) {
        new CLI();
    }
}
