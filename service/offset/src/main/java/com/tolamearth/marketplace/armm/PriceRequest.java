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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tolamearth.marketplace.offset.Nft;
import io.micronaut.core.annotation.Introspected;
import java.util.ArrayList;
import java.util.List;

@Introspected
public class PriceRequest {

  @JsonProperty("nfts")
  private List<NftHolder> nfts;

  public PriceRequest(){
    nfts = new ArrayList<>();
  }

  public PriceRequest(List<NftHolder> nfts) {
    this.nfts = nfts;
  }

  @JsonIgnore
  public List<Nft> actualNfts() {
    return nfts.stream().map(holder -> holder.nft).toList();
  }

  public List<NftHolder> getNfts() {
    return nfts;
  }

  public void setNfts(List<NftHolder> nfts) {
    this.nfts = nfts;
  }

  @Override
  public String toString() {
    return "PriceRequest[" +
        "nfts=" + nfts + ']';
  }

  public static class NftHolder {

    @JsonProperty("nft_id")
    private Nft nft;

    public NftHolder(){}

    public NftHolder(Nft nft) {
      this.nft = nft;
    }

    public Nft getNft() {
      return nft;
    }

    public void setNft(Nft nft) {
      this.nft = nft;
    }
  }
}
