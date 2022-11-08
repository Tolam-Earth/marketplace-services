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

package com.tolamearth.marketplace.offset.job

import com.tolamearth.marketplace.mirrornode.TransactionClient
import com.tolamearth.marketplace.offset.TransactionTypeCode
import com.tolamearth.marketplace.offset.db.ListingTransactionRepo
import com.tolamearth.marketplace.offset.db.PurchasedTransactionRepo
import com.tolamearth.marketplace.offset.job.PendingTransactions.PendingTransaction
import com.tolamearth.marketplace.mirrornode.TransactionClient.MirrorNodeTransactionResponse
import spock.lang.Specification

class RestTransactionValidatorSpec extends Specification {

  private TransactionClient transactionClient = Mock(TransactionClient)
  private ListingTransactionRepo listingTransactionRepo = Mock(ListingTransactionRepo)
  private PurchasedTransactionRepo purchasedTransactionRepo = Mock(PurchasedTransactionRepo)
  private RestTransactionValidator validator = new RestTransactionValidator(transactionClient, listingTransactionRepo, purchasedTransactionRepo)
  private final String TXN_ID = "0.0.47857038@1660578633.201321688"

  def "test that listingTransactionRepo is called #times times when response.transactions is #transactions"() {
    given: "transaction client responds with #transactions"
      transactionClient.getTransaction(_) >> new MirrorNodeTransactionResponse(transactions, null)

    when: "validator is called"
      validator.validate(new PendingTransaction(5L, TXN_ID, TransactionTypeCode.LIST))

    then: "correct repo is called"
      times * listingTransactionRepo.updateById(_, _) >> 1
    and: "incorrect repo is not"
      0 * purchasedTransactionRepo.updateById(_, _) >> 0

    where:
      transactions                                  | times
      null                                          | 0
      []                                            | 0
      [[:]]                                         | 0
      [["consensus_timestamp": null]]               | 0
      [["consensus_timestamp": ""]]                 | 0
      [["consensus_timestamp": "123456789.012345"]] | 1
  }

  def "test that purchasedTransactionRepo is called #times times when response.transactions is #transactions"() {
    given: "transaction client responds with #transactions"
      transactionClient.getTransaction(_) >> new MirrorNodeTransactionResponse(transactions, null)

    when: "validator is called"
      validator.validate(new PendingTransaction(5L, TXN_ID, TransactionTypeCode.PURCHASE))

    then: "correct repo is called"
      times * purchasedTransactionRepo.updateById(_, _) >> 1
    and: "incorrect repo is not"
      0 * listingTransactionRepo.updateById(_, _) >> 0

    where:
      transactions                                  | times
      null                                          | 0
      []                                            | 0
      [[:]]                                         | 0
      [["consensus_timestamp": null]]               | 0
      [["consensus_timestamp": ""]]                 | 0
      [["consensus_timestamp": "123456789.012345"]] | 1
  }
}
