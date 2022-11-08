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

import com.fasterxml.jackson.annotation.JsonProperty;

public record SimpleOffset(@JsonProperty("owner_id") String ownerId, Nft nft, Long price) implements Comparable<SimpleOffset>{

  public SimpleOffset(Offset offset){
    this(offset.ownerId(), offset.nft(), offset.price());
  }

  @Override
  public int compareTo(SimpleOffset o) {
    var tokenParts = nft.tokenId().split("\\.");
    var otherTokenParts = o.nft.tokenId().split("\\.");
    // compare token shard
    var result = Integer.valueOf(tokenParts[0]).compareTo(Integer.valueOf(otherTokenParts[0]));
    if (result != 0) {
      return result;
    }
    // compare token realm
    result = Integer.valueOf(tokenParts[1]).compareTo(Integer.valueOf(otherTokenParts[1]));
    if (result != 0) {
      return result;
    }
    // compare token id
    result = Integer.valueOf(tokenParts[2]).compareTo(Integer.valueOf(otherTokenParts[2]));
    if (result != 0) {
      return result;
    }
    // compare serial number as a final check
    return nft.serialNumber().compareTo(o.nft.serialNumber());


  }
}
