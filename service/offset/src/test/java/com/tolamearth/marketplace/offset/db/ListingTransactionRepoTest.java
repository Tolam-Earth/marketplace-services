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

package com.tolamearth.marketplace.offset.db;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tolamearth.marketplace.common.IntegrationTest;
import com.tolamearth.marketplace.offset.ListingTransactionState;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ListingTransactionRepoTest extends IntegrationTest {

  private static final String VALID_ACCOUNT_ID = "0.0.5";
  private static final String VALID_TRANSACTION_ID = VALID_ACCOUNT_ID + "@1604557331.565419523";

  @Inject
  ListingTransactionRepo listingTransactionRepo;

  @Test
  void testSave() {
    var now = OffsetDateTime.now().toEpochSecond();
    var listedTransaction = new ListingTransaction(VALID_TRANSACTION_ID,
        ListingTransactionState.CREATED, now, now);

    assertEquals(0, listingTransactionRepo.count());

    ListingTransaction savedTransaction = listingTransactionRepo
        .save(listedTransaction);

    var retrievedTransactionOptional = listingTransactionRepo
        .findById(savedTransaction.getId());

    assertTrue(retrievedTransactionOptional.isPresent());

    var retrievedTransaction = retrievedTransactionOptional.get();

    assertAll(() -> assertEquals(1, listingTransactionRepo.count()),
        () -> assertNotNull(retrievedTransaction.getId()),
        () -> assertEquals(VALID_TRANSACTION_ID, retrievedTransaction.getTransactionId()),
        () -> assertEquals(ListingTransactionState.CREATED,
            retrievedTransaction.getListingTransactionState()),
        () -> assertEquals(now, retrievedTransaction.getCreationTime()),
        () -> assertEquals(now, retrievedTransaction.getLastUpdateTime()));
  }
}