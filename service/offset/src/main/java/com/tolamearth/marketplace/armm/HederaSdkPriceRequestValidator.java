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

import com.hedera.hashgraph.sdk.TokenId;
import com.tolamearth.marketplace.offset.Nft;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class HederaSdkPriceRequestValidator implements PriceRequestValidator {

  @Override
  public void validate(PriceRequest request) {
    List<Nft> nftIds = request.actualNfts();
    validateParameter(nftIds, this::validateLength);
    validateParameter(nftIds, this::validateUnique);
    validateParameter(nftIds, this::validateTokenIdFormats);
  }

  private void validateLength(List<Nft> nftIds){
    if (nftIds.size() > 600 || nftIds.size() == 0){
      throw new IllegalArgumentException("Invalid list length");
    }
  }

  private void validateUnique(List<Nft> nftIds) {
    if (nftIds.size() > nftIds.stream().distinct().count()) {
      throw new IllegalArgumentException("duplicate token ids");
    }
  }

  private void validateTokenIdFormats(List<Nft> nftIds) {
    nftIds.forEach(nftId -> {
      try {
        TokenId.fromString(nftId.tokenId());
      } catch (Exception e) {
        throw new IllegalArgumentException("token id parse failed: " + nftId);
      }
    });
  }
}
