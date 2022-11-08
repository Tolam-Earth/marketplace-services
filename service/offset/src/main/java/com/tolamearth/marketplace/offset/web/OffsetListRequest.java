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

package com.tolamearth.marketplace.offset.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tolamearth.marketplace.common.ListingOrder;
import com.tolamearth.marketplace.offset.ListingState;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.QueryValue;

/**
 * @param accountId account id, needs to comply with Hedera account format standards
 * @param tokenId token id, needs to comply with Hedera token format standards
 * @param order sort order, either "asc" or "desc"
 * @param limit maximum number of records to return
 * @param state Filter for nfts in a listed / unlisted or all states
 */
record OffsetListRequest(@QueryValue("acccount_id") @JsonProperty("account_id") @Nullable String accountId, @QueryValue("token_id") @JsonProperty("token_id") @Nullable String tokenId, @QueryValue(defaultValue = "asc") ListingOrder order,  Integer limit, @JsonProperty("list_state") ListingState state) {

}
