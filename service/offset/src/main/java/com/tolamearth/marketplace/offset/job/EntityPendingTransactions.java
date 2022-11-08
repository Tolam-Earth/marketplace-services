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

package com.tolamearth.marketplace.offset.job;

import com.tolamearth.marketplace.offset.ListingTransactionState;
import com.tolamearth.marketplace.offset.TransactionTypeCode;
import com.tolamearth.marketplace.offset.db.ListingTransaction;
import com.tolamearth.marketplace.offset.db.ListingTransactionRepo;
import com.tolamearth.marketplace.offset.db.PurchasedTransaction;
import com.tolamearth.marketplace.offset.db.PurchasedTransactionRepo;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import java.time.Clock;
import java.util.List;
import java.util.stream.Stream;

/**
 * Finds pending transactions by querying and manipulating the entity objects
 *
 * @author Jesse Elliott
 */
@Singleton
public class EntityPendingTransactions implements PendingTransactions {

  private final Clock clock;
  private final Integer finalitySeconds;
  private final ListingTransactionRepo listingTransactionRepo;
  private final PurchasedTransactionRepo purchasedTransactionRepo;

  public EntityPendingTransactions(Clock clock,
      @Value("${hem.txn.minimum-finality:5}") Integer finalitySeconds,
      ListingTransactionRepo listingTransactionRepo,
      PurchasedTransactionRepo purchasedTransactionRepo) {
    this.clock = clock;
    this.finalitySeconds = finalitySeconds;
    this.listingTransactionRepo = listingTransactionRepo;
    this.purchasedTransactionRepo = purchasedTransactionRepo;
  }

  @Override
  public List<PendingTransaction> list() {
    var listingTransactions = loadListingTransactions();
    var purchaseTransactions = loadPurchaseTransactions();
    var pendingListingTransactions = listingTransactions.stream().map(
        transaction -> new PendingTransaction(transaction.getId(), transaction.getTransactionId(), TransactionTypeCode.LIST));
    var pendingPurchaseTransactions = purchaseTransactions.stream().map(
        transaction -> new PendingTransaction(transaction.getId(), transaction.getTxnId(), TransactionTypeCode.PURCHASE));
    return Stream.concat(pendingListingTransactions, pendingPurchaseTransactions).toList();
  }

  private List<ListingTransaction> loadListingTransactions() {
    long time = getCutoffTime();
    return listingTransactionRepo.findByListingTransactionStateAndLastUpdateTimeLessThan(
        ListingTransactionState.APPROVED, time);
  }

  private List<PurchasedTransaction> loadPurchaseTransactions() {
    long time = getCutoffTime();
    return purchasedTransactionRepo.findByPurchasedStateAndLastUpdateTimeLessThan(
        ListingTransactionState.APPROVED, time);
  }

  private long getCutoffTime() {
    return clock.millis() - (finalitySeconds * 1000);
  }
}
