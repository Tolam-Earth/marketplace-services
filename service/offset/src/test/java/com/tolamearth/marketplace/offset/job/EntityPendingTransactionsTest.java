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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tolamearth.marketplace.offset.db.ListingTransaction;
import com.tolamearth.marketplace.offset.db.ListingTransactionRepo;
import com.tolamearth.marketplace.offset.db.PurchasedTransaction;
import com.tolamearth.marketplace.offset.db.PurchasedTransactionRepo;
import com.tolamearth.marketplace.offset.ListingTransactionState;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EntityPendingTransactionsTest {

  private final Instant testInstant = LocalDateTime.of(2022, 7, 1, 12, 0).atZone(ZoneOffset.UTC)
      .toInstant();
  private final Clock clock = Clock.fixed(testInstant, ZoneOffset.UTC);

  private final ListingTransactionRepo transactionRepository =
      mock(ListingTransactionRepo.class);
  private final PurchasedTransactionRepo purchasedTransactionRepo =
      mock(PurchasedTransactionRepo.class);

  @ParameterizedTest()
  @ValueSource(ints = {60, 300, 3600, 525600})
  void timeIsCorrect(int time) {
    var checker = new EntityPendingTransactions(clock, time, transactionRepository,
        purchasedTransactionRepo);

    checker.list();

    verify(transactionRepository)
        .findByListingTransactionStateAndLastUpdateTimeLessThan(
            eq(ListingTransactionState.APPROVED),
            eq(testInstant.toEpochMilli() - (time * 1000L))
        );
  }

  @Test
  void testNoResults() {
    var bean = new EntityPendingTransactions(clock, 300, transactionRepository,
        purchasedTransactionRepo);

    assertEquals(Collections.emptyList(), bean.list());
  }

  @Test
  void testMapping() {
    var bean = new EntityPendingTransactions(clock, 300, transactionRepository,
        purchasedTransactionRepo);

    var transactions = new ArrayList<ListingTransaction>();
    for (int i = 1; i <= 5; i++) {
      transactions.add(new ListingTransaction(i + "", null, null, null));
      transactions.get(i - 1).setId((long) i);
    }

    when(transactionRepository.findByListingTransactionStateAndLastUpdateTimeLessThan(any(),
        eq(testInstant.toEpochMilli() - 300000)))
        .thenReturn(transactions);

    var purchasedTransactions = new ArrayList<PurchasedTransaction>();
    for (int i = 1; i <= 5; i++) {
      purchasedTransactions.add(new PurchasedTransaction(i + 5 + "", null, null, null, null));
      purchasedTransactions.get(i - 1).setId((long) i);
    }

    when(purchasedTransactionRepo.findByPurchasedStateAndLastUpdateTimeLessThan(any(),
        eq(testInstant.toEpochMilli() - 300000)))
        .thenReturn(purchasedTransactions);


    var results = bean.list();

    for (int i = 1; i <= 10; i++) {
      var result = results.get(i - 1);
      assertEquals(i, result.getInternalId() + (switch (result.getTransactionType()) {
        case LIST -> 0;
        case PURCHASE -> 5;
      }));
      assertEquals(i + "", result.getTxnId());
    }
  }
}