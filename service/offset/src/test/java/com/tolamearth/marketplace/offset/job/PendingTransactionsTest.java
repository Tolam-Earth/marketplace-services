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

package com.tolamearth.marketplace.offset.job;

import com.tolamearth.marketplace.common.AssertionPair;
import com.tolamearth.marketplace.offset.TransactionTypeCode;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PendingTransactionsTest {

  @ParameterizedTest
  @DisplayName(
      ""
          + "When getting transaction ID, "
          + "Then return Hedera TransactionID format: <shard.realm.num@sss.nnn>[/nonce][?schedule]")
  @ValueSource(strings = {
      "0.0.2252@1640075693.891386528/1",
      "0.0.9401@1602138343.335616988?schedule"})
  void txnId(String txnId) {
    Assertions.assertEquals(txnId,
        new PendingTransactions.PendingTransaction(0L, txnId, TransactionTypeCode.LIST).getTxnId());
  }

  @ParameterizedTest
  @DisplayName(
      ""
          + "When getting transaction ID, "
          + "Then return Hedera Mirror Node path segment name format: <shard.realm.num-sss-nnn>")
  @MethodSource
  void mirrorNodeTxnId(AssertionPair<String> txnIds) {
    Assertions.assertEquals(txnIds.expected(),
        new PendingTransactions.PendingTransaction(0L, txnIds.actual(), TransactionTypeCode.LIST).getMirrorNodeTxnId());
  }

  private static Stream<AssertionPair<String>> mirrorNodeTxnId() {
    return Stream.of(
        AssertionPair.of(
            "0.0.2252-1640075693-891386528",
            "0.0.2252@1640075693.891386528/1"),
        AssertionPair.of(
            "0.0.9401-1602138343-335616988",
            "0.0.9401@1602138343.335616988?schedule"));
  }
}
