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
import com.tolamearth.marketplace.offset.db.ListingRepo;
import com.tolamearth.marketplace.offset.db.PurchasedTransactionRepo;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Named;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("purchasedCreation")
public class CreationPurchasedExpiryChecker extends AbstractPurchasedExpiryChecker {
  private static final Logger log = LoggerFactory.getLogger(CreationPurchasedExpiryChecker.class);

  public CreationPurchasedExpiryChecker(Clock clock,
      @Value("${hem.purchased_txn_created_expire_timeout:30}") Integer timeoutSeconds,
      PurchasedTransactionRepo transactionRepository, ListingRepo listingRepository) {
    super(clock, timeoutSeconds, transactionRepository, listingRepository);
  }

  @Override
  public ListingTransactionState getState() {
    return ListingTransactionState.CREATED;
  }

  @Override
  Logger getLog() {
    return log;
  }
}
