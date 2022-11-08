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

import com.tolamearth.marketplace.offset.db.ListingRepo;
import com.tolamearth.marketplace.offset.db.PurchasedTransactionRepo;
import org.slf4j.Logger;

import java.time.Clock;

public abstract class AbstractPurchasedExpiryChecker implements PurchasedExpiryChecker {

  private final Integer timeoutSeconds;
  private final Clock clock;
  private final PurchasedTransactionRepo transactionRepository;
  private final ListingRepo listingRepository;

  public AbstractPurchasedExpiryChecker(Clock clock,
      Integer timeoutSeconds,
      PurchasedTransactionRepo transactionRepository,
      ListingRepo listingRepository) {
    this.timeoutSeconds = timeoutSeconds;
    this.clock = clock;
    this.transactionRepository = transactionRepository;
    this.listingRepository = listingRepository;
  }

  public int run() {
    var expiredTime = clock.millis() - (timeoutSeconds * 1000);
    var txnIds = transactionRepository.findTxnIdByPurchasedStateAndLastUpdateTimeLessThan(
        getState(), expiredTime);
    if (txnIds != null && !txnIds.isEmpty()) {
      var listings = listingRepository.findByTxnIdIn(txnIds);
      if(listings != null && !listings.isEmpty()) {
        listings.forEach(listing -> listing.setPurchaseTxnId(null));
        listingRepository.updateAll(listings);
      }
      if(listings.size() < txnIds.size()){
        getLog().warn("Fewer listings were updated than we think should have been -> updated: "
                + listings.size() + ", expected: " + txnIds.size());
      }
      var deletedTransactions = transactionRepository.deleteByTxnIdIn(txnIds);
      if (deletedTransactions < txnIds.size()) {
        getLog().warn("Fewer purchases were deleted than we think should have been -> deleted: "
            + deletedTransactions + ", expected: " + txnIds.size());
      }
      return txnIds.size();
    }
    return 0;
  }

  abstract Logger getLog();
}
