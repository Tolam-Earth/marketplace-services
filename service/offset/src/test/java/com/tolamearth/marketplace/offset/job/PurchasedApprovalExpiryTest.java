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
import java.time.Clock;

public class PurchasedApprovalExpiryTest extends AbstractPurchasedExpiryCheckerTest {

  @Override
  protected AbstractPurchasedExpiryChecker createChecker(Clock clock, Integer timeout,
      PurchasedTransactionRepo transactionRepository,
      ListingRepo listingRepository) {
    return new ApprovalPurchasedExpiryChecker(clock, timeout, transactionRepository, listingRepository);
  }

  @Override
  protected ListingTransactionState getState() {
    return ListingTransactionState.APPROVED;
  }
}
