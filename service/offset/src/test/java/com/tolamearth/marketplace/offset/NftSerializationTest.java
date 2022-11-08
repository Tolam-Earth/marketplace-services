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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tolamearth.marketplace.common.IntegrationTest;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class NftSerializationTest extends IntegrationTest {

  private final String TOKEN_ID = "0.1.2";
  private final long SERIAL_NUMBER = 123L;
  private static final long PRICE = 100L;

  @Test
  void testSerialization(ObjectMapper mapper) throws IOException {
    var original = new PricedNft(TOKEN_ID, SERIAL_NUMBER, PRICE);
    var result = mapper.writeValueAsString(original);
    var deserialized = mapper.readValue(result, PricedNft.class);
    assertEquals(original, deserialized);
  }

  @Test
  void testDeserialization(ObjectMapper mapper) throws IOException {
    var json = String.format("""
            {
            "token_id": "%1$s",
            "serial_number": %2$s,
            "price": %3$s
            }
            """, TOKEN_ID, SERIAL_NUMBER, PRICE);

    var deserialized = mapper.readValue(json, PricedNft.class);
    assertAll(
            () -> assertEquals(TOKEN_ID, deserialized.tokenId()),
            () -> assertEquals(SERIAL_NUMBER, deserialized.serialNumber()),
            () -> assertEquals(PRICE, deserialized.price()));
  }
}