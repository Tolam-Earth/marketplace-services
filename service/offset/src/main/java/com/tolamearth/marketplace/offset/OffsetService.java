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

import com.tolamearth.marketplace.common.HederaAccount;
import com.tolamearth.marketplace.common.ListingOrder;
import java.util.List;

public interface OffsetService {

  List<ListingStateOffset> fetchOffsets(HederaAccount account, String tokenId, Integer limit,
      ListingOrder order, ListingState state);

  List<ListingStateOffset> fetchOffsets(String tokenId, Integer limit, ListingOrder order,
      ListingState state);

    void list(String accountId, String transactionId, List<PricedNft> pricedNfts);

    void purchase(String accountId, String transactionId, List<Nft> nfts);

}
