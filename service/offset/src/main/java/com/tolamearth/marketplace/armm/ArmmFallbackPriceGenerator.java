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

package com.tolamearth.marketplace.armm;

import com.tolamearth.marketplace.armm.ArmmPriceResponse.ArmmNftPrice;
import com.tolamearth.marketplace.offset.Nft;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ArmmFallbackPriceGenerator {

  Map<Nft, ArmmNftPrice> generated = new HashMap<>();

  ArmmNftPrice generateRandomNftPrice(Nft nft) {

    var base = Math.round(Math.random() * 9000 + 1000);
    return generateRandomNftPrice(nft, base);
  }

  ArmmNftPrice generateRandomNftPrice(Nft nft, long base) {

    if (!generated.containsKey(nft)) {
      System.out.println("armm fallback cache miss with " + nft);
      var price = new ArmmNftPrice(nft.tokenId()+"-"+nft.serialNumber(), base - generateRandomPrice(), base + generateRandomPrice(), null, null);
      generated.put(nft, price);
    }
    return generated.get(nft);
  }

  private long generateRandomPrice() {
    return (long) Math.floor(Math.random() * 3 + 1);
  }
}
