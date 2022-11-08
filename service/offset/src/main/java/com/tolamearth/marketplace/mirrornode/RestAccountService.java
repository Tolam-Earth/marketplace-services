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

package com.tolamearth.marketplace.mirrornode;

import com.tolamearth.marketplace.common.HederaAccount;
import com.tolamearth.marketplace.common.ListingOrder;
import com.tolamearth.marketplace.offset.Nft;
import com.tolamearth.marketplace.offset.SimpleOffset;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
class RestAccountService implements AccountService {

  private final MirrorNodeClient mirrorNodeClient;

  public RestAccountService(MirrorNodeClient mirrorNodeClient) {
    this.mirrorNodeClient = mirrorNodeClient;
  }

  @Override
  public Boolean isAccountValid(HederaAccount account) {
    return mirrorNodeClient.getAccount(account.getId()).accounts().size() > 0;
  }

  @Override
  public List<SimpleOffset> fetchOffsets(HederaAccount account, String tokenFilter, Integer limit,
      ListingOrder order) {
    if (tokenFilter != null && !tokenFilter.startsWith("gt") && !tokenFilter.startsWith("lt")) { // TODO: validate that this guard statement is valid
      throw new IllegalArgumentException("tokenId is not valid filter");
    }
    List<MirrorNodeNft> tokens = mirrorNodeClient.getTokens(account.getId(),
            order.name().toLowerCase(), limit, tokenFilter)
        .nfts();
    return tokens.stream().map(token -> new SimpleOffset(token.accountId(), new Nft(token.tokenId(), token.serialNumber()),null )).toList();

  }

}
