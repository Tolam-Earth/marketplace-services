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

import com.tolamearth.marketplace.common.HederaConfigurationProperties;
import com.tolamearth.marketplace.common.IntegrationTest;
import com.tolamearth.marketplace.common.error.ApiErrorTest;
import com.tolamearth.marketplace.mirrornode.MirrorNodeNft;
import com.tolamearth.marketplace.mirrornode.TokenClient;
import com.tolamearth.marketplace.mirrornode.AccountService;
import com.tolamearth.marketplace.offset.Nft;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PurchaseNftsTest  extends IntegrationTest implements ApiErrorTest {
    @Inject
    HederaConfigurationProperties hederaConfig;
    @Inject
    @Client("/")
    HttpClient client;
    private static final String VALID_ACCOUNT_ID = "0.0.34750426";
    private static final String VALID_TRANSACTION_ID = VALID_ACCOUNT_ID + "@1658439475.570410603";
    private static final String VALID_TOKEN_ID = "0.0.47767226";
    private static final Long VALID_SERIAL_NUMBER = 3L;
    private static final Long VALID_PRICE = 1L;

    @Inject
    ListingRepo listingRepo;
    @MockBean
    @Replaces(TokenClient.class)
    TokenClient tokenClient = mock(TokenClient.class);

    @BeforeEach
    void setupTokenCall(){
      when(tokenClient.getNft(any(), any())).thenReturn(new MirrorNodeNft(VALID_ACCOUNT_ID, null, null, null, null, VALID_SERIAL_NUMBER, VALID_TOKEN_ID));
    }

    @Inject
    AccountService accountService;
    @MockBean(bean = AccountService.class)
    AccountService accountService() {
        return mock(AccountService.class);
    }

    @Override
    public HttpClient getClient() {
        return client;
    }
    private static List<Nft> createValidNftList() {
        return List.of(new Nft(VALID_TOKEN_ID, VALID_SERIAL_NUMBER));
    }
    private static List<PricedNft> createValidPricedNftList() {
        return List.of(new PricedNft(VALID_TOKEN_ID, VALID_SERIAL_NUMBER, VALID_PRICE));
    }

    @Test
    void purchaseNfts(){
        Mockito.when(accountService.isAccountValid(any())).thenReturn(true);
        var listingUri = UriBuilder.of("/hem/v1/offsets/list").build();
        BlockingHttpClient blockingHttpClient = getClient().toBlocking();
        //Add a listing
        ListingRequest listingRequest = new ListingRequest(VALID_TRANSACTION_ID, VALID_ACCOUNT_ID, createValidPricedNftList());
        HttpResponse<ListingResponse> listingResponse = blockingHttpClient.exchange(HttpRequest.POST(listingUri, listingRequest), ListingResponse.class);
        //Purchase the listing
        var purchaseUri = UriBuilder.of("/hem/v1/offsets/purchase").build();
        PurchaseRequest request = new PurchaseRequest(VALID_TRANSACTION_ID, VALID_ACCOUNT_ID, createValidNftList());
        HttpResponse<PurchaseResponse> response = blockingHttpClient.exchange(HttpRequest.POST(purchaseUri, request), PurchaseResponse.class);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(request, response.body().request());
    }
}
