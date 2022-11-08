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

import static com.tolamearth.marketplace.offset.ListingTransactionState.APPROVED;
import static java.time.ZoneId.systemDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tolamearth.marketplace.mirrornode.TokenClient;
import com.tolamearth.marketplace.offset.ListingTransactionState;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class OffsetRepoTest {

  private final ListingRepo listingRepo = mock(ListingRepo.class);
  private final ListingTransactionRepo transactionRepo = mock(ListingTransactionRepo.class);
  private final PurchasedTransactionRepo purchasedTransactionRepo = mock(PurchasedTransactionRepo.class);
  private final TokenClient tokenClient = mock(TokenClient.class);
  private final LocalDateTime testDateTime = LocalDateTime.of(2022, 7, 1, 12, 0);
  private final Instant testInstant = testDateTime
      .atZone(systemDefault())
      .toInstant();
  private final Clock clock = Clock.fixed(testInstant, systemDefault());
  private final OffsetRepo repo = new OffsetRepo(transactionRepo, purchasedTransactionRepo, listingRepo,
      tokenClient, clock);


  @Test
  void testApproveListing() {
    var previousTimestamp = LocalDateTime.of(2022, 7, 1, 11, 59, 45)
        .atZone(systemDefault()).toInstant().toEpochMilli();
    when(transactionRepo.getByTransactionId(any())).then(invocation -> {
      var tr = new ListingTransaction("0.0.1", ListingTransactionState.CREATED, previousTimestamp,
          previousTimestamp);
      tr.setId(5L);
      return tr;
    });

    repo.approveListing("");

    var transactionArgumentCaptor = ArgumentCaptor.forClass(ListingTransaction.class);
    verify(transactionRepo).update(transactionArgumentCaptor.capture());

    var transaction = transactionArgumentCaptor.getValue();
    assertEquals(5L, transaction.getId());
    assertEquals(APPROVED, transaction.getListingTransactionState());
    assertEquals(previousTimestamp, transaction.getCreationTime());
    assertNotEquals(previousTimestamp, transaction.getLastUpdateTime(),
        "lastUpdateTime not updated");
    assertEquals(testInstant.toEpochMilli(), transaction.getLastUpdateTime(),
        "lastUpdateTime updated not using clock");


    /*verify(transactionRepo).update(argThat(transaction -> {
          assertEquals(5L, transaction.getId());
          assertEquals(APPROVED, transaction.getListingTransactionState());
          assertEquals(previousTimestamp, transaction.getCreationTime());
          assertNotEquals(previousTimestamp, transaction.getLastUpdateTime(),
              "lastUpdateTime not updated");
          assertEquals(testInstant.toEpochMilli(), transaction.getLastUpdateTime(),
              "lastUpdateTime updated not using clock");

          return true;
        }
    ));*/

  }
}
