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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tolamearth.marketplace.common.IntegrationTest;
import com.tolamearth.marketplace.common.error.ApiErrorTest;
import com.tolamearth.marketplace.common.error.HemErrorCode;
import com.tolamearth.marketplace.mirrornode.MirrorNodeNft;
import com.tolamearth.marketplace.mirrornode.TokenClient;
import com.tolamearth.marketplace.offset.ListingTransactionState;
import com.tolamearth.marketplace.offset.PricedNft;
import com.tolamearth.marketplace.offset.db.ListingRepo;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class OffsetControllerCreateListingTest extends IntegrationTest implements ApiErrorTest {

  private static final String BASE_URI = "/hem/v1/offsets/";
  private static final String URI = BASE_URI + "list";
  private static final String VALID_ACCOUNT_ID = "0.0.34750426";
  private static final String VALID_TRANSACTION_ID = VALID_ACCOUNT_ID + "@1658439475.570410603";
  private static final String VALID_TOKEN_ID = "0.0.47767226";
  private static final Long VALID_SERIAL_NUMBER = 3L;
  private static final Long VALID_PRICE = 1L;
  @Inject
  @Client("/")
  HttpClient client;
  @Inject
  ListingRepo listingRepo;
  @SuppressWarnings("unused")
  @MethodSource
  private static Stream<ListingRequest> createListingInvalidParameters() {
    return Stream.of(new ListingRequest(null, VALID_ACCOUNT_ID, createValidPricedNftList()),
        new ListingRequest(VALID_TRANSACTION_ID, null, createValidPricedNftList()),
        new ListingRequest(VALID_TRANSACTION_ID, VALID_ACCOUNT_ID, null));
  }

  private static List<PricedNft> createValidPricedNftList() {
    return List.of(new PricedNft(VALID_TOKEN_ID, VALID_SERIAL_NUMBER, VALID_PRICE));
  }

  @MockBean
  @Replaces(TokenClient.class)
  TokenClient tokenClient = mock(TokenClient.class);

  @BeforeEach
  void setupTokenCall(){
    when(tokenClient.getNft(any(), any())).thenReturn(new MirrorNodeNft(VALID_ACCOUNT_ID, null, null, null, null, VALID_SERIAL_NUMBER, VALID_TOKEN_ID));
  }

  @AfterEach
  void cleanup() {
    listingRepo.deleteAll();
  }

  @ParameterizedTest
  @MethodSource
  @NullSource
  @DisplayName("passing invalid parameters when creating listing should fail")
  void createListingInvalidParameters(ListingRequest request) {
    var uri = UriBuilder.of(URI).build();
    assertErrorCode(HttpRequest.POST(uri, request), HemErrorCode.MISSING_REQUIRED_FIELD);
  }

  @Test
  void createListing() {
    var uri = UriBuilder.of(URI).build();
    BlockingHttpClient blockingHttpClient = getClient().toBlocking();
    ListingRequest request = new ListingRequest(VALID_TRANSACTION_ID,
        VALID_ACCOUNT_ID, createValidPricedNftList());
    HttpResponse<ListingResponse> response = blockingHttpClient.exchange(
        HttpRequest.POST(uri, request), ListingResponse.class);

    assertEquals(HttpStatus.CREATED, response.getStatus());
    assertEquals(request, response.body().request());
  }

  @Test
  void verifyListingStatus() {

    var uri = UriBuilder.of(URI).build();
    BlockingHttpClient blockingHttpClient = getClient().toBlocking();

    ListingRequest request = new ListingRequest(VALID_TRANSACTION_ID,
        VALID_ACCOUNT_ID, createValidPricedNftList());
    HttpResponse<ListingResponse> response = blockingHttpClient.exchange(
        HttpRequest.POST(uri, request), ListingResponse.class);
    assertEquals(HttpStatus.CREATED, response.getStatus());
    assertEquals(request, response.body().request());

    var transactionUri = UriBuilder.of(
        BASE_URI + "/txn?txn_id=" + VALID_TRANSACTION_ID + "&txn_type=LIST").build();
    HttpResponse<RetrieveTransactionRecordInfoResponse> transactionResponse =
        blockingHttpClient.exchange(HttpRequest.GET(transactionUri),
            RetrieveTransactionRecordInfoResponse.class);

    Assertions.assertEquals(ListingTransactionState.APPROVED,
        transactionResponse.body().state());
  }

  @Override
  public HttpClient getClient() {
    return client;
  }
}
