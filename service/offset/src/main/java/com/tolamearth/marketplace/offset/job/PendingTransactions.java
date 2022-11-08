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

import com.tolamearth.marketplace.offset.TransactionTypeCode;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holder for approved, valid transactions
 *
 * @author Jesse Elliott
 */
public interface PendingTransactions {

  /**
   * {@return a list of pending transactions, empty list if none exist}
   */
  List<PendingTransaction> list();

  /**
   * Record representing the minimum information to validate a transaction
   */
  class PendingTransaction {

    private static final Pattern HEDERA_TXN_ID = Pattern.compile(
        "(\\d+\\.\\d+\\.\\d+)@(\\d+\\.\\d+)(?:/\\d+)?(?:/?\\?schedule)?", Pattern.CASE_INSENSITIVE);
    private final long internalId;
    private final String txnId;
    private final TransactionTypeCode transactionType;

    /**
     * @param internalId ID of the internal object
     * @param txnId      transaction id
     * @param transactionType transaction type
     */
    public PendingTransaction(long internalId, String txnId, TransactionTypeCode transactionType) {
      this.internalId = internalId;
      this.txnId = txnId;
      this.transactionType = transactionType;
    }

    public long getInternalId() {
      return internalId;
    }

    /**
     * @return the Hedera TransactionID, formatted as ({@literal <shard.realm.num@sss.nnn>[/nonce][?schedule]}).
     * See <a href="https://docs.hedera.com/guides/docs/sdks/transactions/transaction-id">Hedera API
     * Docs</a>.
     */
    public String getTxnId() {
      return txnId;
    }


    /**
     * @return Mirror Node Rest API Path Segment TransactionID, formatted as ({@literal
     * <shard.realm.num-sss-nnn>}). See <a href="https://github.com/hashgraph/hedera-mirror-node/blob/main/docs/rest/README.md">
     * Hedera Mirror Mode API Docs</a>/
     */
     public String getMirrorNodeTxnId() {
      Matcher matcher = HEDERA_TXN_ID.matcher(txnId);
      if (matcher.matches()) {
        return matcher.group(1) + "-" + matcher.group(2).replace('.', '-');
      } else {
        throw new IllegalArgumentException("Unexpected Hedera TransactionId: " + txnId);
      }
    }

    public TransactionTypeCode getTransactionType() {
      return transactionType;
    }
  }
}
