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

import com.tolamearth.marketplace.mirrornode.TransactionClient;
import com.tolamearth.marketplace.offset.ListingTransactionState;
import com.tolamearth.marketplace.offset.db.ListingTransactionRepo;
import com.tolamearth.marketplace.offset.db.PurchasedTransactionRepo;
import com.tolamearth.marketplace.offset.job.PendingTransactions.PendingTransaction;
import jakarta.inject.Singleton;

@Singleton
public class RestTransactionValidator implements TransactionValidator {

  private final TransactionClient client;
  private final ListingTransactionRepo listingTransactionRepo;
  private final PurchasedTransactionRepo purchasedTransactionRepo;

  public RestTransactionValidator(TransactionClient client,
      ListingTransactionRepo listingTransactionRepo,
      PurchasedTransactionRepo purchasedTransactionRepo) {
    this.client = client;
    this.listingTransactionRepo = listingTransactionRepo;
    this.purchasedTransactionRepo = purchasedTransactionRepo;
  }

  @Override
  public boolean validate(PendingTransaction pendingTransaction) {
    var transactionResponse = client.getTransaction(pendingTransaction.getMirrorNodeTxnId());
    var transactionList = transactionResponse == null ? null : transactionResponse.transactions();
    if (transactionList != null && !transactionList.isEmpty()) {
      var transaction = transactionList.get(0);
      if (transaction.containsKey("consensus_timestamp")) {
        var consensusTimestamp = (String) transaction.get("consensus_timestamp");
        if (consensusTimestamp != null && !consensusTimestamp.isEmpty()) {
          return switch (pendingTransaction.getTransactionType()) {
            case LIST ->
                listingTransactionRepo.updateById(pendingTransaction.getInternalId(), ListingTransactionState.LISTED) > 0;
            case PURCHASE -> purchasedTransactionRepo.updateById(pendingTransaction.getInternalId(),
                ListingTransactionState.PURCHASED) > 0;
          };
        }
      }
    }
    return false;
  }
}
