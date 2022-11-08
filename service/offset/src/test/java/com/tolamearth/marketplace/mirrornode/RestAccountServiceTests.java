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

package com.tolamearth.marketplace.mirrornode;


import static com.tolamearth.marketplace.common.ListingOrder.ASC;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tolamearth.marketplace.common.HederaAccount;
import com.tolamearth.marketplace.mirrornode.MirrorNodeClient.MirrorNodeAccountResponse;
import com.tolamearth.marketplace.mirrornode.MirrorNodeClient.MirrorNodeTokenResponse;

import io.micronaut.http.client.exceptions.HttpClientException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RestAccountServiceTests {

  private final MirrorNodeClient mockClient = mock(MirrorNodeClient.class);
  private final AccountService service = new RestAccountService(mockClient);

  @Test
  @DisplayName("isAccountValid returns false on an empty response")
  void testIsAccountValidWithNoResults() {
    when(mockClient.getAccount(any())).thenReturn(
        new MirrorNodeAccountResponse(emptyList(), null));
    assertFalse(service.isAccountValid(new HederaAccount("0.0.1")));
  }

  @Test
  @DisplayName("isAccountValid returns true when there's something in the list")
  void testIsAccountValidWithResults() {
    var account = Map.of("accountId", "0.0.1");
    when(mockClient.getAccount(any())).thenReturn(
        new MirrorNodeAccountResponse(List.of(account), null));
    assertTrue(service.isAccountValid(new HederaAccount("0.0.1")));
  }

  @Test
  @DisplayName("isAccountValid lets exception bubble up")
  void testIsAccountValidWithException() {
    when(mockClient.getAccount(any())).thenThrow(new HttpClientException("test"));
    try {
      service.isAccountValid(new HederaAccount("0.0.1"));
      fail("Exception should bubble up");
    } catch (Exception e) {
      assertEquals(HttpClientException.class, e.getClass());
    }
  }

  @Test
  @DisplayName("fetchOffsets returns empty list on an empty response")
  void testFetchOffsetsWithNoResults() {
    when(mockClient.getTokens(any(), any(), any(), any())).thenReturn(
        new MirrorNodeTokenResponse(emptyList(), null));
    var result = service.fetchOffsets(new HederaAccount("0.0.1"), null, 25, ASC);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("fetchOffsets returns offsets when there's something in the list")
  void testFetchOffsetsWithResults() {
    when(mockClient.getTokens(any(),any(), any(), any())).thenReturn(
        new MirrorNodeTokenResponse(List.of(new MirrorNodeNft("0.0.1", null, null, null, null, 2L, "0.0.2")), null));
    var result = service.fetchOffsets(new HederaAccount("0.0.1"), null, 25, ASC);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals("0.0.2", result.get(0).nft().tokenId());
    assertEquals("0.0.1", result.get(0).ownerId());
  }

  @Test
  @DisplayName("fetchOffsets lets exception bubble up")
  void testFetchOffsetsWithException() {
    when(mockClient.getTokens(any(),any(), any(), any())).thenThrow(
        new HttpClientException("test"));
    try {
      service.fetchOffsets(new HederaAccount("0.0.1"), null, 25, ASC);
      fail("Exception should bubble up");
    } catch (Exception e) {
      assertEquals(HttpClientException.class, e.getClass());
    }
  }

  @Test
  @DisplayName("fetchOffsets lets exception bubble up")
  void testFetchOffsetsWithInvalidTokenId() {
    try {
      service.fetchOffsets(new HederaAccount("0.0.1"), "0.0.1", 25, ASC);
      fail("Exception should bubble up");
    } catch (Exception e) {
      assertEquals(IllegalArgumentException.class, e.getClass());
    }
  }
}
