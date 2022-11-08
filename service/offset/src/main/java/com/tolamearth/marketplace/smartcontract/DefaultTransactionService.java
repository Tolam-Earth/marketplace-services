/*
 * Copyright 2022 Tolam Earth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tolamearth.marketplace.smartcontract;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.ContractFunctionParameters;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.tolamearth.marketplace.common.HederaConfigurationProperties;
import com.tolamearth.marketplace.common.LogFormatter;
import com.tolamearth.marketplace.offset.Nft;
import com.tolamearth.marketplace.offset.PricedNft;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Primary
public class DefaultTransactionService implements TransactionService {

  private static final Logger log = LoggerFactory.getLogger(DefaultTransactionService.class);

  private final HederaConfigurationProperties hederaConfigurationProperties;
  private final Client hederaClient;
  private final LogFormatter logFormatter;

  public DefaultTransactionService(
      HederaConfigurationProperties hederaConfigurationProperties, Client hederaClient,
      LogFormatter logFormatter) {
    this.hederaConfigurationProperties = hederaConfigurationProperties;
    this.hederaClient = hederaClient;
    this.logFormatter = logFormatter;
  }

  private ContractExecuteTransaction createContractExecuteTransaction(String functionName) {
    String contractId = hederaConfigurationProperties.offsetsContractId();
    Long contractFees = hederaConfigurationProperties.gasAmount();
    log.info("Using contract ID {} (\"{}\") with {} hbar in fees",
        contractId,
        functionName,
        logFormatter.arg(contractFees));
    return new ContractExecuteTransaction()
        .setContractId(ContractId.fromString(contractId))
        .setGas(hederaConfigurationProperties.gasAmount());
  }

  protected final String toSolidityAddress(String tokenId) {
    return TokenId.fromString(tokenId).toSolidityAddress();
  }

  public void allowList(String accountId, String transactionId, List<PricedNft> pricedNfts) {
    var tokenIds = pricedNfts.stream().map(PricedNft::tokenId).map(this::toSolidityAddress)
        .toArray(String[]::new);
    var prices = pricedNfts.stream().map(PricedNft::price).map(BigInteger::valueOf)
        .toArray(BigInteger[]::new);
    var serialNumbers = pricedNfts.stream().mapToLong(PricedNft::serialNumber).toArray();
    var transaction = createContractExecuteTransaction("whitelist_list");
    executeContract(transaction
        .setFunction("whitelist_list", new ContractFunctionParameters()
            .addAddress(AccountId.fromString(accountId).toSolidityAddress())
            .addAddressArray(tokenIds)
            .addInt64Array(serialNumbers)
            .addUint256Array(prices)
        ));
  }

  public void purchaseList(String accountId, String transactionId, List<Nft> nfts) {
    var tokenIds = nfts.stream().map(Nft::tokenId).map(this::toSolidityAddress)
        .toArray(String[]::new);
    var serialNumbers = nfts.stream().mapToLong(Nft::serialNumber).toArray();
    var transaction = createContractExecuteTransaction("whitelist_purchase");
    executeContract(transaction
        .setFunction("whitelist_purchase", new ContractFunctionParameters()
            .addAddress(AccountId.fromString(accountId).toSolidityAddress())
            .addAddressArray(tokenIds)
            .addInt64Array(serialNumbers)
        ));
  }

  <T extends Transaction<T>>void executeContract (Transaction < T > transaction) {
      transaction.freezeWith(hederaClient);
      log.info("Attempting to execute {}", transaction.getTransactionId());
      try {
        TransactionResponse response = transaction.execute(hederaClient);
        log.info(response.getReceipt(hederaClient).toString());
        log.info("Successfully executed {}", transaction.getTransactionId());
      } catch (TimeoutException | PrecheckStatusException | ReceiptStatusException e) {
        throw new RuntimeException(e);
      }
    }
  }
