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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tolamearth.marketplace.common.IntegrationTest;
import com.tolamearth.marketplace.offset.PricedNft;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateOffsetListingRequestTest extends IntegrationTest {

  private static final String TRANSACTION_ID = "0.0.5@1604557331.565419523";
  private static final String ACCOUNT_ID = "0.0.5";
  private static final String TOKEN_ID = "0.0.12";
  private static final long SERIAL_NUMBER = 1234;
  private static final long PRICE = 100L;

  @Test
  void testSerialization(ObjectMapper mapper) throws IOException {
    var original = new ListingRequest(TRANSACTION_ID, ACCOUNT_ID,
        List.of(new PricedNft(TOKEN_ID, SERIAL_NUMBER,PRICE)));
    String result = mapper.writeValueAsString(original);
    var deserialized = mapper.readValue(result,
        ListingRequest.class);
    assertEquals(original, deserialized);
  }

  @Test
  void testDeserialization(ObjectMapper mapper) throws IOException {
    String json = String.format("""
        {
          "txn_id": "%1$s",
          "account_id": "%2$s",
          "nfts": [{
            "token_id": "%3$s",
            "serial_number": %4$s,
            "price": %5$s
          }]
        }
        """, TRANSACTION_ID, ACCOUNT_ID, TOKEN_ID, SERIAL_NUMBER, PRICE);

    var deserialized = mapper.readValue(json, ListingRequest.class);

    assertAll(
        () -> assertEquals(TRANSACTION_ID, deserialized.transactionId()),
        () -> assertEquals(ACCOUNT_ID, deserialized.accountId()),
        () -> assertEquals(List.of(new PricedNft(TOKEN_ID, SERIAL_NUMBER, PRICE)), deserialized.pricedNfts()));
  }
}