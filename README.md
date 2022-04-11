# Java code to interact with Hedera in TESTNET

## Before you start

### Accounts
Using your operator account and private (Set them in [Accounts](./src/main/java/org/tj/hedera22/Accounts.java)), create two other accounts
for the participants and update the Accounts.java file.
Use [CreateAccount](./src/main/java/org/tj/hedera22/CreateAccount.java) to create them

### Deploy the contracts
Use [DeployERC20](./src/main/java/org/tj/hedera22/DeployERC20.java) twice with the relevant token info (name, supply) and
[DeployDLOBEx](./src/main/java/org/tj/hedera22/DeployDLOBEx.java) for the main smart contract.  
Note the deployment uses testnet and use your operator account.

### Transfer some native HBARs to the participants
For the participants to be able to interact with the native chain, transfer some native HBARs to them using
[TransferHBARs](./src/main/java/org/tj/hedera22/TransferHBARs.java).  
You can then use [CLI](./src/main/java/org/tj/hedera22/CLI.java) the with the option "Display balances" to confirm.

### Transfer some tokens to the participants.
Same as above, but using [TransferTokens](./src/main/java/org/tj/hedera22/TransferTokens.java)

You should see on the CLI:

```text
[main] INFO org.tj.hedera22.CLI - Displaying balances... (gas used: 320000)
[main] INFO org.tj.hedera22.CLI -  Operator: 9560.53196754 ℏ
[main] INFO org.tj.hedera22.CLI -  Participant 1: 200 ℏ, HHBAR Owned: 50000, HUSD Owned: 50000, HHBAR Allowance: 0, HUSD Allowance: 0
[main] INFO org.tj.hedera22.CLI -  Participant 2: 200 ℏ, HHBAR Owned: 50000, HUSD Owned: 50000, HHBAR Allowance: 0, HUSD Allowance: 0
```

### Approve
The next step is to allow the smart contract [DeployDLOBEx](./src/main/java/org/tj/hedera22/DeployDLOBEx.java) to spend your tokens post trade.  
This is done with [ApproveTransfer](./src/main/java/org/tj/hedera22/ApproveTransfer.java).

Finally, the last state from the CLI, you should also see the allowance:

```text
[main] INFO org.tj.hedera22.CLI - Displaying balances... (gas used: 320000)
[main] INFO org.tj.hedera22.CLI -  Operator: 9552.52838234 ℏ
[main] INFO org.tj.hedera22.CLI -  Participant 1: 199.27539722 ℏ, HHBAR Owned: 50000, HUSD Owned: 50000, HHBAR Allowance: 50000, HUSD Allowance: 50000
[main] INFO org.tj.hedera22.CLI -  Participant 2: 199.27539722 ℏ, HHBAR Owned: 50000, HUSD Owned: 50000, HHBAR Allowance: 50000, HUSD Allowance: 50000
```

## CLI
Just launch the CLI and enjoy

```text
[main] INFO org.tj.hedera22.CLI - Hedera Menu. Please select an option:
[main] INFO org.tj.hedera22.CLI -  Acting account: 0.0.7599 (Operator)
[main] INFO org.tj.hedera22.CLI -    1. Exit
[main] INFO org.tj.hedera22.CLI -    2. Allow Trading (~ 1ℏ)
[main] INFO org.tj.hedera22.CLI -    3. Stop Trading (~ 1ℏ)
[main] INFO org.tj.hedera22.CLI -    4. Add All Participants (~ 2ℏ)
[main] INFO org.tj.hedera22.CLI -    5. Select participant (free)
[main] INFO org.tj.hedera22.CLI -    6. Display order book (~ 7ℏ)
[main] INFO org.tj.hedera22.CLI -    7. Place Limit Order (~ 1ℏ)
[main] INFO org.tj.hedera22.CLI -    8. Place Market Order (~ 1ℏ)
[main] INFO org.tj.hedera22.CLI -    9. Display balances (~ 8ℏ)
[main] INFO org.tj.hedera22.CLI -    10. Display trading allowed status (~ 0.1ℏ)
[main] INFO org.tj.hedera22.CLI -    11. Display latest debug (~ 0ℏ)
[main] INFO org.tj.hedera22.CLI -    12. Reset
```
