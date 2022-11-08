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

import com.tolamearth.marketplace.armm.PriceRequest.NftHolder;
import com.tolamearth.marketplace.offset.Nft;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ArmmService {

  private final ArmmClient armmClient;

  public ArmmService(ArmmClient armmClient) {
    this.armmClient = armmClient;
  }

  public List<NftPrice> getPrices(List<Nft> nfts) {
    var req = new PriceRequest(nfts.stream().map(NftHolder::new).toList());
    var priceObject = armmClient.getPrices(req).toPriceResponse();
    if (priceObject != null) {
      var prices = priceObject.prices();
      if (prices != null && !prices.isEmpty()) {
        return priceObject.prices();
      }
    }
    return new ArrayList<>();
  }

}
