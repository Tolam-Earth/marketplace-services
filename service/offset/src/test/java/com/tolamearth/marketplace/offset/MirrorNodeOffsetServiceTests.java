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

package com.tolamearth.marketplace.offset;

import static com.tolamearth.marketplace.common.ListingOrder.ASC;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tolamearth.marketplace.common.HederaAccount;
import com.tolamearth.marketplace.common.error.HemException;
import com.tolamearth.marketplace.mirrornode.AccountService;
import com.tolamearth.marketplace.offset.db.OffsetRepo;
import com.tolamearth.marketplace.smartcontract.TransactionService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MirrorNodeOffsetServiceTests {

  private final AccountService mockAccountService = mock(AccountService.class);
  private final OffsetRepo offsetRepo = mock(OffsetRepo.class);
    private final TransactionService transactionService = mock(TransactionService.class);

  private final HederaAccount account = new HederaAccount("0.0.1");
  private final Integer limit = 3;

  private final List<List<SimpleOffset>> mirrorNodeResponses = List.of(
      List.of(OffsetTestUtil.createOffset(account.getId(), 2, 100L), OffsetTestUtil.createOffset(account.getId(), 3, 100L),
          OffsetTestUtil.createOffset(account.getId(), 4, 100L)),
      List.of(OffsetTestUtil.createOffset(account.getId(), 5, 100L), OffsetTestUtil.createOffset(account.getId(), 6, 100L),
          OffsetTestUtil.createOffset(account.getId(), 7, 100L)),
      List.of(OffsetTestUtil.createOffset(account.getId(), 8, 100L), OffsetTestUtil.createOffset(account.getId(), 9, 100L),
          OffsetTestUtil.createOffset(account.getId(), 10, 100L))
 );

  private final MirrorNodeOffsetService service =
      new MirrorNodeOffsetService(mockAccountService, offsetRepo, transactionService);

  @Test
  @DisplayName("An invalid account will throw an error")
  void testInvalidAccountThrowsException() {
    when(mockAccountService.isAccountValid(any())).thenReturn(false);
    try {
      service.fetchOffsets(account, null, limit, ASC, ListingState.ALL);
      fail("Exception should have been thrown");
    } catch (Exception e) {
      assertEquals(HemException.class, e.getClass());
      var hemCode = ((HemException) e).getCode();
      assertEquals(1004, hemCode.getCode());
    }
  }

  @Test
  @DisplayName("A valid account with no nfts will return an empty list")
  void testNoTokensReturned() {
    when(mockAccountService.isAccountValid(any())).thenReturn(true);
    when(mockAccountService.fetchOffsets(any(), any(), any(), any())).thenReturn(emptyList());

    List<ListingStateOffset> result = service.fetchOffsets(account, null, limit, ASC, ListingState.ALL);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  // TODO: Rewrite the following tests to fit refactored service
//  @ParameterizedTest(name = "A valid account with nfts but no listings will return {0} results when state is {1}")
//  @CsvSource({"3,ALL", "0,LISTED", "3,UNLISTED"})
//  void testNoListingsReturned(Integer expectedResults, ListingState state) {
//    setupTokenCalls();
//
//    List<ListingStateOffset> result = service.fetchOffsets(account, null, limit, ASC, state);
//    assertNotNull(result);
//    assertEquals(expectedResults, result.size());
//    if (expectedResults > 0) { // make sure that loop is skipped when limit is satisfied
//      verify(mockAccountService).fetchOffsets(any(), any(), any(), any());
//    } else {
//      verify(mockAccountService, times(4)).fetchOffsets(any(), any(), any(), any());
//    }
//  }

//  @ParameterizedTest(name = "A valid account with nfts but no listings will return {0} results when state is {1}")
//  @CsvSource({"2,ALL", "0,LISTED", "2,UNLISTED"})
//  void testLessOffsetsThanLimitReturned(Integer expectedResults, ListingState state) {
//    when(mockAccountService.isAccountValid(any())).thenReturn(true);
//    when(mockAccountService.fetchOffsets(any(), any(), any(), any())).thenReturn(
//        mirrorNodeResponses.get(0).subList(0, 2));
//
//    List<ListingStateOffset> result = service.fetchOffsets(account, null, limit, ASC, state);
//    assertNotNull(result);
//    assertEquals(expectedResults, result.size());
//  }
//
//  @Test
//  @DisplayName("A valid account with listings will display the listed subset up to the limit")
//  void testListedOffsets() {
//    setupTokenCalls();
//
//    setupListingReturns();
//
//    List<ListingStateOffset> result = service.fetchOffsets(account, null, limit, ASC, LISTED);
//    assertNotNull(result);
//    assertEquals(3, result.size());
//    // check that listed offset out of the limit range is not in the list
//    assertTrue(result.stream()
//        .noneMatch(
//            listingStateOffset -> listingStateOffset.offset().nft().tokenId().equals("0.0.9")));
//    verify(mockAccountService, times(3)).fetchOffsets(any(), any(), any(), any());
//  }
//
//  @Test
//  @DisplayName("A valid account with less listings than the limit will display the whole listed subset")
//  void testLessListedOffsetsThanLimit() {
//    setupTokenCalls();
//
//    when(offsetRepo.findListingByTokenIdIn(any())).thenReturn(
//        List.of(makeListing(1L, "0.0.2", 2L, account.getId())),
//        List.of(makeListing(2L, "0.0.8", 8L, account.getId()))
//    );
//
//    List<ListingStateOffset> result = service.fetchOffsets(account, null, limit, ASC, LISTED);
//    assertNotNull(result);
//    assertEquals(2, result.size());
//    verify(mockAccountService, times(4)).fetchOffsets(any(), any(), any(), any());
//  }
//
//  @Test
//  @DisplayName("A valid account with listings will display the unlisted subset up to the limit")
//  void testUnlistedOffsets() {
//    setupTokenCalls();
//
//    setupListingReturns();
//
//    List<ListingStateOffset> result = service.fetchOffsets(account, null, limit, ASC, UNLISTED);
//    assertNotNull(result);
//    assertEquals(3, result.size());
//    // check that unlisted offset out of the limit range is not in the list
//    assertTrue(result.stream()
//        .noneMatch(
//            listingStateOffset -> listingStateOffset.offset().nft().tokenId().equals("0.0.7")));
//    verify(mockAccountService, times(2)).fetchOffsets(any(), any(), any(), any());
//  }

  private void setupTokenCalls() {
    when(mockAccountService.isAccountValid(any())).thenReturn(true);
    when(mockAccountService.fetchOffsets(any(), any(), any(), any())).thenReturn(
        mirrorNodeResponses.get(0),
        mirrorNodeResponses.get(1),
        mirrorNodeResponses.get(2),
        new ArrayList<>());
  }

  private void setupListingReturns() {
    when(offsetRepo.findListingByTokenIdIn(any())).thenReturn(
        List.of(makeListing(4L, "0.0.2", 2L, account.getId())),
        List.of(makeListing(5L, "0.0.5", 5L, account.getId())),
        List.of(
            makeListing(6L, "0.0.8", 8L, account.getId()),
            makeListing(7L, "0.0.9", 8L, account.getId())
        )
    );
  }

  private OffsetListing makeListing(Long id, String tokenId, Long serialNumber, String accountId) {
    return new OffsetListing(id, tokenId, serialNumber, accountId, null, 100L, null, null, null,
        null, null);
  }
}

