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

package com.tolamearth.marketplace.offset.integration

import com.tolamearth.marketplace.mirrornode.TransactionClient
import com.tolamearth.marketplace.mirrornode.TransactionClient.MirrorNodeTransactionResponse
import com.tolamearth.marketplace.offset.ListingState
import com.tolamearth.marketplace.offset.Nft
import com.tolamearth.marketplace.offset.Offset
import com.tolamearth.marketplace.offset.job.PendingTransactions.PendingTransaction
import com.tolamearth.marketplace.offset.TransactionTypeCode
import com.tolamearth.marketplace.offset.db.OffsetRepo
import com.tolamearth.marketplace.util.IntegrationSpec
import io.micronaut.context.annotation.Replaces
import io.micronaut.test.annotation.MockBean
import jakarta.inject.Inject

class TransactionMessageHandlerPublishingSpec extends IntegrationSpec {

  private static final String TXN_ID = "0.0.444555666@123456.123"

  @MockBean
  @Replaces(TransactionClient)
  TransactionClient transactionClient = Mock TransactionClient
  @MockBean
  @Replaces(OffsetRepo)
  OffsetRepo offsetRepo = Mock OffsetRepo

  @Inject
  TransactionMessageHandler handler

  def setup(){
    // Pubsub emulator needs time to set up
    // TODO: handle this in a better way
    sleep(5000)
  }

  def "test that list publish works"() {
    given: "there is a transaction"
      def transaction = new PendingTransaction(5, TXN_ID, TransactionTypeCode.LIST)
    and: "txn id finds an offset"
      offsetRepo.findAllByListingTransactionId(TXN_ID) >> [new Offset("0.0.1", new Nft("0.0.10", 5), 5000, ListingState.LISTED, null)]
    and: "transaction client returns correctly"
      transactionClient.getTransaction(transaction.mirrorNodeTxnId) >> new MirrorNodeTransactionResponse([["consensus_timestamp": "123456.123456"]], null)

    when: "call is made"
      def result = handler.publish([transaction])
      println result
    then: "listing is present"
      result[0]
    and: "purchase is null"
      !result[1]
  }
}
