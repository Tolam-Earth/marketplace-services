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

package com.tolamearth.marketplace.smartcontract.web;

import static org.junit.jupiter.api.Assertions.*;

import com.tolamearth.marketplace.common.HederaConfigurationProperties;
import com.tolamearth.marketplace.common.IntegrationTest;
import com.tolamearth.marketplace.common.error.ApiErrorTest;
import com.tolamearth.marketplace.smartcontract.web.ConversionController.ConversionRateResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import jakarta.inject.Inject;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class ConversionControllerTest extends IntegrationTest implements ApiErrorTest {
  @Inject
  HederaConfigurationProperties hederaConfig;
  @Inject
  @Client("/")
  HttpClient client;

  @Override
  public HttpClient getClient() {
    return client;
  }

  @Test
  void testGetTinybarPerCent() {
    var conversionUri = UriBuilder.of("/hem/v1/conversion").build();
    var blockingHttpClient = getClient().toBlocking();

    var response = blockingHttpClient.exchange(HttpRequest.GET(conversionUri),
        ConversionRateResponse.class);
    assertAll(
        () -> assertEquals(HttpStatus.OK, response.getStatus()),
        () -> assertEquals(BigInteger.valueOf(1000000), response.body().rate())
    );
  }
}