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

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.client.annotation.Client;
import java.util.List;

@Client("mirror-node")
@Header(name = "Authorization", value = "${hem.lworks.api-key}")
public interface TokenClient {

  @Get("/tokens/{tokenId}/nfts/{serialNumber}")
  public MirrorNodeNft getNft(@PathVariable String tokenId, @PathVariable Long serialNumber);

  record MirrorNodeNftList(List<MirrorNodeNft> nfts, List<Object> links){}
}
