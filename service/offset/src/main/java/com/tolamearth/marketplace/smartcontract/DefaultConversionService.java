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

package com.tolamearth.marketplace.smartcontract;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractCallQuery;
import com.hedera.hashgraph.sdk.ContractFunctionResult;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.tolamearth.marketplace.common.HederaConfigurationProperties;
import com.tolamearth.marketplace.common.LogFormatter;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;
import java.math.BigInteger;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Primary
public class DefaultConversionService implements ConversionService {
  private static final Logger log = LoggerFactory.getLogger(DefaultConversionService.class);

  private final HederaConfigurationProperties hederaConfigurationProperties;
  private final Client hederaClient;
  private final LogFormatter logFormatter;

  public DefaultConversionService(HederaConfigurationProperties hederaConfigurationProperties, Client hederaClient,
      LogFormatter logFormatter) {
    this.hederaConfigurationProperties = hederaConfigurationProperties;
    this.hederaClient = hederaClient;
    this.logFormatter = logFormatter;
  }

  private ContractFunctionResult executeContractCallQuery(String functionName)
      throws PrecheckStatusException, TimeoutException {
    String contractId = hederaConfigurationProperties.offsetsContractId();
    Long queryPayment = hederaConfigurationProperties.queryPaymentHbar();

    log.info("Querying contract ID {} (\"{}\") with {} hbar in fees",
        contractId,
        functionName,
        logFormatter.arg(queryPayment));

    return new ContractCallQuery()
        .setContractId(ContractId.fromString(contractId))
        .setGas(hederaConfigurationProperties.gasAmount())
        .setFunction(functionName)
        .setQueryPayment(new Hbar(queryPayment))
        .execute(hederaClient);
  }

  public BigInteger getTinybarPerCent() {
    final String GET_TINYBAR_PER_CENT = "getTinybarPerCent";
    try {
      return executeContractCallQuery(GET_TINYBAR_PER_CENT).getUint256(0);
    } catch (PrecheckStatusException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }
}
