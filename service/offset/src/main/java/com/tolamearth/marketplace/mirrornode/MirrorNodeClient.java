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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;
import java.util.List;
import java.util.Map;

@Client("mirror-node")
@Header(name = "Authorization", value = "${hem.lworks.api-key}")
interface MirrorNodeClient {

    @Get("/accounts")
    MirrorNodeAccountResponse getAccount(@QueryValue("account.id") String accountId);

    @Get("/accounts/{accountId}/nfts")
    MirrorNodeTokenResponse getTokens(@PathVariable String accountId,
                  @Nullable @QueryValue String order,
                  @Nullable @QueryValue Integer limit,
                  @Nullable @QueryValue("token.id") String tokenFilter);

    record MirrorNodeTokenResponse(List<MirrorNodeNft> nfts, Map links){}
    record MirrorNodeAccountResponse(List<Map> accounts, Map links){};


    enum TokenType {
        FUNGIBLE_COMMON, NON_FUNGIBLE_UNIQUE
    }
}
