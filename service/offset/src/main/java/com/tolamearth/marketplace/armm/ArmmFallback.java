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

import io.micronaut.context.annotation.Requires;
import io.micronaut.retry.annotation.Fallback;

@Fallback
@Requires(property = "hem.armm.fallback", value = "true")
public class ArmmFallback implements ArmmClient {

  private final ArmmFallbackPriceGenerator priceGenerator;

  public ArmmFallback(ArmmFallbackPriceGenerator priceGenerator) {
    this.priceGenerator = priceGenerator;
  }


  @Override
  public ArmmPriceResponse getPrices(PriceRequest request) {
    var nftPrices = request.actualNfts().stream().map(priceGenerator::generateRandomNftPrice).toList();
    return new ArmmPriceResponse(request.getNfts(), nftPrices);
  }


}
