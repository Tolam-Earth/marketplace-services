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

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.TransactionId;
import com.tolamearth.marketplace.offset.Nft;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Objects;

@Singleton
@Primary
public class DefaultPurchaseRequestValidator implements PurchaseRequestValidator {

  @Override
  public void validate(PurchaseRequest request) {
    validateParameter(request.transactionId(), TransactionId::fromString);
    validateParameter(request.accountId(), AccountId::fromString);
    validateParameter(request.nfts(), Objects::requireNonNull);
    validateParameter(request.nfts(), this::validateNfts);
  }

  void validateNfts(List<Nft> nfts) {
    if (nfts.isEmpty()) {
      throw new IllegalArgumentException("nfts must contain at least one entry");
    }
  }
}
