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

import com.tolamearth.integration.marketplace.MarketplaceEvent
import com.tolamearth.integration.marketplace.MarketplaceEvent.EventType
import com.tolamearth.marketplace.mirrornode.TransactionClient
import com.tolamearth.marketplace.mirrornode.TransactionClient.MirrorNodeTransactionResponse
import com.tolamearth.marketplace.offset.ListingState
import com.tolamearth.marketplace.offset.Nft
import com.tolamearth.marketplace.offset.Offset
import com.tolamearth.marketplace.offset.job.PendingTransactions.PendingTransaction
import com.tolamearth.marketplace.offset.ListingTransactionState
import com.tolamearth.marketplace.offset.OffsetListing
import com.tolamearth.marketplace.offset.TransactionTypeCode
import com.tolamearth.marketplace.offset.db.OffsetRepo

import spock.lang.Specification

class TransactionMessageHandlerSpec extends Specification {

  private static final String TXN_ID = "0.0.444555666@123456.123"

  OffsetRepo repo = Mock OffsetRepo
  TransactionClient client = Mock TransactionClient
  MarketplaceEventPublisher publisher = Mock MarketplaceEventPublisher

  TransactionMessageHandler handler = new TransactionMessageHandler(repo, client, publisher)

  def "test that nothing is called if no transactions present"() {
    when:
      handler.publish([])
    then:
      0 * repo.findAllByListingTransactionId(_)
      0 * repo.findAllByPurchaseTransactionId(_)
  }

  def "test that transactions call the right repos"() {

    when:
      handler.publish(input)
    then:
      listCalls * repo.findAllByListingTransactionId(_) >> []
      purchaseCalls * repo.findAllByPurchaseTransactionId(_) >> []

    where:
      input                                                              | listCalls | purchaseCalls
      [new PendingTransaction(5L, TXN_ID, TransactionTypeCode.LIST)]     | 1         | 0
      [new PendingTransaction(5L, TXN_ID, TransactionTypeCode.PURCHASE)] | 0         | 1
  }

  def "test that list transaction binds correctly to event"() {
    given: "there is one listed transaction"
      def transactions = [new PendingTransaction(5L, TXN_ID, TransactionTypeCode.LIST)]
    and: "there is an associated Offset"
      def offset = new Offset("0.0.1", new Nft("0.0.123", 5), 5000, ListingState.LISTED, [])
      repo.findAllByListingTransactionId(TXN_ID) >> [offset]
    and: "the mirror node responds with a value"
      def mirrorNodeTransaction = ["consensus_timestamp": "12346.12346"]
      client.getTransaction(_) >> new MirrorNodeTransactionResponse([mirrorNodeTransaction], null)
    and: "the event is passed"
      MarketplaceEvent event
    when: "handler is called"
      handler.publish(transactions)

    then: "the event is passed to the publisher"
      1 * publisher.publish(_) >> { List<MarketplaceEvent> args ->
        event = args[0];
      }
    and: "all fields are as expected"
      verifyAll {
        event.getEventType() == EventType.LISTED
        def transaction = event.getTransactions(0)
        transaction.transactionId == TXN_ID
        transaction.nftId.tokenId == offset.nft().tokenId()
        transaction.nftId.serialNumber == offset.nft().serialNumber().toString()
        transaction.listPrice == offset.price()
        !transaction.purchasePrice

      }
  }

  def "test that purchase transaction binds correctly to event"() {
    given: "there is one purchased transaction"
      def transactions = [new PendingTransaction(5L, TXN_ID, TransactionTypeCode.PURCHASE)]
    and: "there is an associated Offset"
      def offset = new Offset("0.0.1", new Nft("0.0.123", 5), null, ListingState.UNLISTED, [new OffsetListing(null, null, null, "0.0.2", null, 5000, null, TXN_ID, ListingTransactionState.PURCHASED, null, null)])
      repo.findAllByPurchaseTransactionId(TXN_ID) >> [offset]
    and: "the mirror node responds with a value"
      def mirrorNodeTransaction = ["consensus_timestamp": "12346.12346"]
      client.getTransaction(_) >> new MirrorNodeTransactionResponse([mirrorNodeTransaction], null)
    and: "the event is passed"
      MarketplaceEvent event
    when: "handler is called"
      handler.publish(transactions)

    then: "the event is passed to the publisher"
      1 * publisher.publish(_) >> { List<MarketplaceEvent> args ->
        event = args[0];
      }
    and: "all fields are as expected"
      verifyAll {
        event.getEventType() == EventType.PURCHASED
        def transaction = event.getTransactions(0)
        transaction.transactionId == TXN_ID
        transaction.nftId.tokenId == offset.nft().tokenId()
        transaction.nftId.serialNumber == offset.nft().serialNumber().toString()
        transaction.listPrice == offset.listings()[0].retailPrice()
        transaction.purchasePrice == offset.listings()[0].retailPrice()

      }
  }

}
