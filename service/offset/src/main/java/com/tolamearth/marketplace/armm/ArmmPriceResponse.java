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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tolamearth.marketplace.armm.PriceRequest.NftHolder;
import com.tolamearth.marketplace.common.error.HemErrorCode;
import com.tolamearth.marketplace.common.error.HemException;
import com.tolamearth.marketplace.offset.Nft;
import java.util.List;

public class ArmmPriceResponse {
  @JsonProperty("request")
  List<NftHolder> request;
  List<ArmmNftPrice> prices;

  public ArmmPriceResponse() {
  }

  public ArmmPriceResponse(List<NftHolder> request, List<ArmmNftPrice> prices) {
    this.request = request;
    this.prices = prices;
  }

  public List<NftHolder> getRequest() {
    return request;
  }

  public void setRequest(List<NftHolder> request) {
    this.request = request;
  }

  public List<ArmmNftPrice> getPrices() {
    return prices;
  }

  public void setPrices(List<ArmmNftPrice> prices) {
    this.prices = prices;
  }

  static class ArmmNftPrice {

    @JsonProperty("nft_id")
    private String nftId;
    @JsonProperty("min_price")
    private Long minPrice;
    @JsonProperty("max_price")
    private Long maxPrice;
    private String code;
    private String message;

    public ArmmNftPrice() {
    }

    public ArmmNftPrice(String nftId, Long minPrice, Long maxPrice, String code, String message) {
      this.nftId = nftId;
      this.minPrice = minPrice;
      this.maxPrice = maxPrice;
      this.code = code;
      this.message = message;
    }

    public String getNftId() {
      return nftId;
    }

    public void setNftId(String nftId) {
      this.nftId = nftId;
    }

    public Long getMinPrice() {
      return minPrice;
    }

    public void setMinPrice(Long minPrice) {
      this.minPrice = minPrice;
    }

    public Long getMaxPrice() {
      return maxPrice;
    }

    public void setMaxPrice(Long maxPrice) {
      this.maxPrice = maxPrice;
    }

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }

  PriceResponse toPriceResponse(){
    var newPrices = prices.stream().map(armmNftPrice -> {
      var split = armmNftPrice.nftId.split("-");
      Nft nft;
      if (split.length == 2){
        nft = new Nft(split[0], Long.parseLong(split[1]));
      }
      else if (split.length == 3){
        nft = new Nft(split[0]+split[1], Long.parseLong(split[2]));
      }
      else{
        throw new HemException(HemErrorCode.INVALID_DATA);
      }
      return new NftPrice(nft, armmNftPrice.minPrice,armmNftPrice.maxPrice, armmNftPrice.code, armmNftPrice.message);
    }).toList();
    return new PriceResponse(new PriceRequest(request), newPrices);
  }
}
