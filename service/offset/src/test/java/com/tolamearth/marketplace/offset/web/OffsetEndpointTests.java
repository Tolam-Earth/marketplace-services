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

package com.tolamearth.marketplace.offset.web;

import static com.tolamearth.marketplace.common.ListingOrder.DESC;
import static com.tolamearth.marketplace.common.error.HemErrorCode.INVALID_DATA;
import static com.tolamearth.marketplace.common.error.HemErrorCode.UNKNOWN_RESOURCE;
import static io.micronaut.http.HttpRequest.GET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tolamearth.marketplace.common.HederaAccount;
import com.tolamearth.marketplace.common.IntegrationTest;
import com.tolamearth.marketplace.common.error.ApiErrorTest;
import com.tolamearth.marketplace.common.error.HemException;
import com.tolamearth.marketplace.offset.ListingState;
import com.tolamearth.marketplace.offset.ListingStateOffset;
import com.tolamearth.marketplace.offset.OffsetService;
import com.tolamearth.marketplace.offset.OffsetTestUtil;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OffsetEndpointTests extends IntegrationTest implements ApiErrorTest {

  public static final String ACCOUNT_ID_PARAMETER = "account_id";
  private final String endpoint = "/hem/v1/offsets";
  private static final long PRICE = 100;
  private final List<ListingStateOffset> definedOffsets = List.of(
      new ListingStateOffset(ListingState.UNLISTED, OffsetTestUtil.createOffset("0.0.2", 2, PRICE)),
      new ListingStateOffset(ListingState.UNLISTED, OffsetTestUtil.createOffset("0.0.2", 4, PRICE)),
      new ListingStateOffset(ListingState.LISTED, OffsetTestUtil.createOffset("0.0.2", 6, PRICE)),
      new ListingStateOffset(ListingState.LISTED, OffsetTestUtil.createOffset("0.0.2", 8, PRICE)),
      new ListingStateOffset(ListingState.UNLISTED, OffsetTestUtil.createOffset("0.0.2", 0, PRICE)));

  @Inject
  @Client("/")
  HttpClient client;

  @Inject
  OffsetService offsetService;

  @Test
  @DisplayName("A valid account with no offsets will get a 200 with no results")
  void testSuccessWithoutResults() {
    URI uri = UriBuilder.of(endpoint)
        .queryParam(ACCOUNT_ID_PARAMETER, "0.0.1")
        .build();

    OffsetListResponse body = client.toBlocking().retrieve(GET(uri), OffsetListResponse.class);

    assertNotNull(body);
    assertNotNull(body.offsets());
    assertTrue(body.offsets().isEmpty());
  }

  @Test
  @DisplayName("A valid account with offsets will get a 200 with results")
  void testSuccessWithResults() {
    String accountId = "0.0.2";
    URI uri = UriBuilder.of(endpoint)
        .queryParam(ACCOUNT_ID_PARAMETER, accountId)
        .build();
    when(offsetService.fetchOffsets(eq(new HederaAccount(accountId)), any(), any(), any(),
        eq(ListingState.ALL)))
        .then(invocation -> definedOffsets);

    OffsetListResponse body = client.toBlocking().retrieve(GET(uri), OffsetListResponse.class);

    assertNotNull(body);
    assertFalse(body.offsets().isEmpty());
    assertEquals(5, body.offsets().size());
  }

  @Test
  @DisplayName("The limit parameter works")
  void testSuccessWithLimitedResults() {
    String accountId = "0.0.2";
    URI uri = UriBuilder.of(endpoint)
        .queryParam(ACCOUNT_ID_PARAMETER, accountId)
        .queryParam("limit", 3)
        .build();

    when(offsetService.fetchOffsets(eq(new HederaAccount(accountId)), any(), eq(3), any(),
        eq(ListingState.ALL)))
        .then(invocation -> definedOffsets.subList(0, 3));

    OffsetListResponse body = client.toBlocking().retrieve(GET(uri), OffsetListResponse.class);
    assertNotNull(body);
    assertFalse(body.offsets().isEmpty());
    assertEquals(3, body.offsets().size());
  }

  @ParameterizedTest(name = "The sort order param binds correctly with {0}")
  @DisplayName("Test sort parameter")
  @CsvSource({"desc", "DESC"})
  void testSuccessWithSortedResults(String sortOrder) {
    String accountId = "0.0.2";
    URI uri = UriBuilder.of(endpoint)
        .queryParam(ACCOUNT_ID_PARAMETER, "0.0.2")
        .queryParam("order", sortOrder)
        .build();

    when(offsetService.fetchOffsets(eq(new HederaAccount(accountId)), any(), any(), eq(DESC),
        eq(ListingState.ALL)))
        .then(invocation -> {
          var list = new ArrayList<>(definedOffsets);
          Collections.reverse(list);
          return list;
        });

    OffsetListResponse body = client.toBlocking().retrieve(GET(uri), OffsetListResponse.class);

    assertNotNull(body);
    assertFalse(body.offsets().isEmpty());
    var offset = body.offsets().get(0).offset();
    assertEquals("0.0.2", offset.ownerId());
    assertEquals("0.0.0", offset.nft().tokenId());
    assertEquals(0, offset.nft().serialNumber());
  }

  @Test
  @DisplayName("An incorrectly formatted account id throws the appropriate error")
  void testMalformedAccountId() {
    URI uri = UriBuilder.of(endpoint)
        .queryParam(ACCOUNT_ID_PARAMETER, "123456")
        .build();
    assertErrorCode(uri, INVALID_DATA);
  }

  @Test
  @DisplayName("An account that doesn't exist throws the appropriate error")
  void testInvalidAccountId() { // correctly formed but not valid
    String accountId = "0.0.3";
    URI uri = UriBuilder.of(endpoint)
        .queryParam(ACCOUNT_ID_PARAMETER, accountId)
        .build();

    when(offsetService.fetchOffsets(eq(new HederaAccount(accountId)), any(), any(), any(),
        eq(ListingState.ALL)))
        .thenThrow(new HemException(UNKNOWN_RESOURCE));
    assertErrorCode(uri, UNKNOWN_RESOURCE);
  }

  @Test
  @DisplayName("An incorrectly formatted order throws the appropriate error")
  void testMalformedOrder() {
    URI uri = UriBuilder.of(endpoint)
        .queryParam(ACCOUNT_ID_PARAMETER, "0.0.2")
        .queryParam("order", "down")
        .build();

    assertErrorCode(uri, INVALID_DATA);
  }

  @Test
  @DisplayName("An incorrectly formatted limit throws the appropriate error")
  void testMalformedLimit() {
    URI uri = UriBuilder.of(endpoint)
        .queryParam(ACCOUNT_ID_PARAMETER, "0.0.2")
        .queryParam("limit", 3.5)
        .build();

    assertErrorCode(uri, INVALID_DATA);
  }

  // TODO: Add tests for token error codes

  @Override
  public HttpClient getClient() {
    return client;
  }

  @MockBean(bean = OffsetService.class)
  OffsetService offsetService() {
    return mock(OffsetService.class);
  }
}
