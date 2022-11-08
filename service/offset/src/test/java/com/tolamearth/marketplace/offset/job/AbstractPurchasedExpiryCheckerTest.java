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

import static java.time.ZoneId.systemDefault;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.tolamearth.marketplace.offset.ListingTransactionState;
import com.tolamearth.marketplace.offset.db.ListingRepo;
import com.tolamearth.marketplace.offset.db.PurchasedTransactionRepo;
import com.tolamearth.marketplace.common.CustomAssertions;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public abstract class AbstractPurchasedExpiryCheckerTest {
  private final Instant testInstant = LocalDateTime.of(2022, 7, 1, 12, 0).atZone(systemDefault()).toInstant();
  private final Clock clock = Clock.fixed(testInstant, systemDefault());
  private final PurchasedTransactionRepo transactionRepository =
      mock(PurchasedTransactionRepo.class);
  private final ListingRepo listingRepository = mock(ListingRepo.class);

  private final List<String> idList = List.of("1", "2");


  @ParameterizedTest()
  @ValueSource(ints = {60, 300, 3600, 525600})
  void checkerSendsRightTime(int time) {
    var checker = createChecker(clock, time, transactionRepository, listingRepository);

    checker.run();

    verify(
        transactionRepository).findTxnIdByPurchasedStateAndLastUpdateTimeLessThan(
        eq(getState()),
        eq(testInstant.toEpochMilli() - (time * 1000L)));
  }

  // different db drivers / repository impls might return null / empty list

  @Test
  void testNullReturnSkipsDeletes() {
    var checker = createChecker(clock, 30, transactionRepository, listingRepository);

    when(transactionRepository.findTxnIdByPurchasedStateAndLastUpdateTimeLessThan(
        eq(getState()), any())).thenReturn(null);

    checker.run();

    verify(transactionRepository, times(0)).deleteByTxnIdIn(any());
  }

  @Test
  void testEmptyReturnSkipsDeletes() {
    var checker = createChecker(clock, 30, transactionRepository, listingRepository);

    when(transactionRepository.findTxnIdByPurchasedStateAndLastUpdateTimeLessThan(
        eq(getState()), any())).thenReturn(new ArrayList<>());

    checker.run();

    verify(transactionRepository, times(0)).deleteByTxnIdIn(any());
  }

  @Test
  void testInteractions() {
    AbstractPurchasedExpiryChecker checker = createChecker(clock, 30,
        transactionRepository, listingRepository);

    when(transactionRepository.findTxnIdByPurchasedStateAndLastUpdateTimeLessThan(
        eq(getState()), any())).thenReturn(idList);
    when(transactionRepository.deleteByTxnIdIn(eq(idList))).thenReturn(idList.size());

    var result = checker.run();

    verify(transactionRepository).deleteByTxnIdIn(eq(idList));
    assertEquals(idList.size(), result);
  }

  // The following two tests are on the line of being integration tests
  @Disabled
  @Test
  void testTransactionLog() {
    AbstractPurchasedExpiryChecker checker = createChecker(clock, 30,
        transactionRepository, listingRepository);

    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    ((Logger) checker.getLog()).addAppender(listAppender);

    when(transactionRepository.findTxnIdByPurchasedStateAndLastUpdateTimeLessThan(
        eq(getState()), any())).thenReturn(idList);
    when(transactionRepository.deleteByTxnIdIn(eq(idList))).thenReturn(1);
    when(listingRepository.deleteByTxnIdIn(eq(idList))).thenReturn(2);

    assertEquals(idList.size(), checker.run());

    var logs = listAppender.list;
    assertEquals(1, logs.size());
    CustomAssertions.assertContains("Fewer purchases", logs.get(0).getMessage());
  }

  @Disabled
  @Test
  void testListingLog() {
    AbstractPurchasedExpiryChecker checker = createChecker(clock, 30,
        transactionRepository, listingRepository);

    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    ((Logger) checker.getLog()).addAppender(listAppender);

    when(transactionRepository.findTxnIdByPurchasedStateAndLastUpdateTimeLessThan(
        eq(getState()), any())).thenReturn(idList);
    when(transactionRepository.deleteByTxnIdIn(eq(idList))).thenReturn(2);

    checker.run();

    var logs = listAppender.list;
    assertEquals(1, logs.size());
    CustomAssertions.assertContains("Fewer listings", logs.get(0).getMessage());
  }

  protected abstract AbstractPurchasedExpiryChecker createChecker(Clock clock, Integer timeout,
      PurchasedTransactionRepo transactionRepository,
      ListingRepo listingRepository);

  protected abstract ListingTransactionState getState();
}
