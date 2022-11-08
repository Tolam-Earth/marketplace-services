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
import com.tolamearth.marketplace.offset.db.ListingTransactionRepo;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractExpiryChecker implements ExpiryChecker {

  private static final Logger log = LoggerFactory.getLogger(AbstractExpiryChecker.class);

  private final Integer timeoutSeconds;
  private final Clock clock;
  private final ListingTransactionRepo transactionRepository;
  private final ListingRepo listingRepository;

  public AbstractExpiryChecker(Clock clock,
      Integer timeoutSeconds,
      ListingTransactionRepo transactionRepository,
      ListingRepo listingRepository) {
    this.timeoutSeconds = timeoutSeconds;
    this.clock = clock;
    this.transactionRepository = transactionRepository;
    this.listingRepository = listingRepository;
  }

  public int run() {
    var expiredTime = clock.millis() - (timeoutSeconds * 1000);
    var txnIds = transactionRepository.findTransactionIdByListingTransactionStateAndLastUpdateTimeLessThan(
        getState(), expiredTime);
    if (txnIds != null && !txnIds.isEmpty()) {
      log.info("Removing expired transactions: {}", txnIds);
      var deletedTransactions = transactionRepository.deleteByTransactionIdIn(txnIds);
      var deletedListings = listingRepository.deleteByTxnIdIn(txnIds);
      if (deletedListings < txnIds.size()) {
        getLog().warn("Fewer listings were deleted than we think should have been -> deleted: "
            + deletedListings + ", expected: " + txnIds.size());
      }
      if (deletedTransactions < txnIds.size()) {
        getLog().warn("Fewer transactions were deleted than we think should have been -> deleted: "
            + deletedTransactions + ", expected: " + txnIds.size());
      }

      return txnIds.size();
    }
    return 0;
  }

  abstract Logger getLog();
}
