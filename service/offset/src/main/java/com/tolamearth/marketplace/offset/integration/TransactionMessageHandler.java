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

package com.tolamearth.marketplace.offset.integration;

import com.google.protobuf.Timestamp;
import com.tolamearth.integration.marketplace.MarketplaceEvent;
import com.tolamearth.integration.marketplace.MarketplaceEvent.EventType;
import com.tolamearth.integration.marketplace.MarketplaceEvent.NftId;
import com.tolamearth.integration.marketplace.MarketplaceEvent.Transaction;
import com.tolamearth.marketplace.mirrornode.TransactionClient;
import com.tolamearth.marketplace.offset.Offset;
import com.tolamearth.marketplace.offset.TransactionTypeCode;
import com.tolamearth.marketplace.offset.db.OffsetRepo;
import com.tolamearth.marketplace.offset.job.PendingTransactions.PendingTransaction;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Singleton
public class TransactionMessageHandler {

  private final OffsetRepo offsetRepo;
  private final TransactionClient transactionClient;
  private final MarketplaceEventPublisher marketplaceEventPublisher;

  public TransactionMessageHandler(OffsetRepo offsetRepo, TransactionClient transactionClient,
      MarketplaceEventPublisher marketplaceEventPublisher) {
    this.offsetRepo = offsetRepo;
    this.transactionClient = transactionClient;
    this.marketplaceEventPublisher = marketplaceEventPublisher;
  }

  public List<String> publish(List<PendingTransaction> transactions) {
    var listingResult = publishEvent(transactions, TransactionTypeCode.LIST, EventType.LISTED,
        this::buildListTransaction);
    var purchaseResult = publishEvent(transactions, TransactionTypeCode.PURCHASE, EventType.PURCHASED,
        this::buildPurchaseTransaction);
    return Arrays.asList(listingResult, purchaseResult);
  }

  private String publishEvent(List<PendingTransaction> transactions, TransactionTypeCode typeCode,
      EventType eventType, Function<PendingTransaction, List<Transaction>> mapper) {
    var mappedTransactions = transactions.stream()
        .filter(tr -> tr.getTransactionType() == typeCode)
        .map(mapper)
        .flatMap(Collection::stream)
        .toList();
    if (mappedTransactions.size() > 0) {
      var purchaseEvent = MarketplaceEvent.newBuilder()
          .setEventType(eventType)
          .addAllTransactions(mappedTransactions)
          .build();
      return marketplaceEventPublisher.publish(purchaseEvent);
    }
    return null;
  }

  private List<Transaction> buildListTransaction(PendingTransaction transaction) {

    return offsetRepo.findAllByListingTransactionId(transaction.getTxnId()).stream()
        .map(offset -> Transaction.newBuilder()
            .setNftId(buildNftId(offset))
            .setOwner(offset.ownerId())
            .setListPrice(offset.price())
            .setTransactionId(transaction.getTxnId())
            .setTransactionTime(buildTimestamp(transaction.getMirrorNodeTxnId()))
            .build())
        .toList();
  }

  private List<Transaction> buildPurchaseTransaction(PendingTransaction transaction) {
    return offsetRepo.findAllByPurchaseTransactionId(transaction.getTxnId()).stream()
        .map(offset -> {
          var listing = offset.listings().stream()
              .filter(it -> it.purchaseTxnId().equals(transaction.getTxnId()))
              .findFirst()
              .orElseThrow();
          return Transaction.newBuilder()
              .setNftId(buildNftId(offset))
              .setOwner(offset.ownerId())
              .setListPrice(listing.retailPrice())
              .setPurchasePrice(listing.retailPrice())
              .setTransactionId(transaction.getTxnId())
              .setTransactionTime(buildTimestamp(transaction.getMirrorNodeTxnId()))
              .build();
        })
        .toList();
  }

  private NftId buildNftId(Offset offset) {
    return NftId.newBuilder()
        .setTokenId(offset.nft().tokenId())
        .setSerialNumber(offset.nft().serialNumber().toString())
        .build();
  }

  private Timestamp buildTimestamp(String mirrorNodeTxnId) {
    var hederaTxn = transactionClient.getTransaction(mirrorNodeTxnId)
        .transactions().get(0);
    var timestamp = (String) hederaTxn.get("consensus_timestamp");
    return Timestamp.newBuilder()
        .setSeconds(Long.parseLong(timestamp.split("\\.")[0]))
        .setNanos(Integer.parseInt(timestamp.split("\\.")[1]))
        .build();
  }
}
