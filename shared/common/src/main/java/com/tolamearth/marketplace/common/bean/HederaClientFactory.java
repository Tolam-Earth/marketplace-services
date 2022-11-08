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

package com.tolamearth.marketplace.common.bean;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.tolamearth.marketplace.common.HederaConfigurationProperties;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

@Factory
public class HederaClientFactory {

  @Singleton
  Client hederaClient(HederaConfigurationProperties hederaConfigurationProperties){
    var operatorId = AccountId.fromString(hederaConfigurationProperties.operatorId());
    var privateKey = PrivateKey.fromString(hederaConfigurationProperties.privateKey());
    var client = Client.forName(hederaConfigurationProperties.network());
    client.setOperator(operatorId, privateKey);
    return client;
  }

}
