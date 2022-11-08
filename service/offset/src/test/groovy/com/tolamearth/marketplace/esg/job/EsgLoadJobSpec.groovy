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

package com.tolamearth.marketplace.esg.job

import com.fasterxml.jackson.databind.ObjectMapper
import com.tolamearth.marketplace.esg.EsgClient
import com.tolamearth.marketplace.esg.EsgClient.EsgAttributes
import com.tolamearth.marketplace.esg.EsgLoadQueue
import com.tolamearth.marketplace.esg.db.EsgOffsetRepo
import com.tolamearth.marketplace.offset.Nft
import com.tolamearth.marketplace.esg.db.EsgOffset
import spock.lang.Specification

class EsgLoadJobSpec extends Specification {

  EsgClient client = Mock EsgClient
  EsgLoadQueue queue = Mock EsgLoadQueue
  EsgOffsetRepo repo = Mock EsgOffsetRepo
  ObjectMapper objectMapper = Mock ObjectMapper
  EsgLoadJob job = new EsgLoadJob(client, queue, repo, objectMapper)

  def "test that an empty queue is a no-op"() {
    given: "there are no actionable tokens in the queue"
      queue.list() >> []
    when:
      job.run()
    then: "no operations are taken"
      0 * queue.lock(_)
      0 * repo.findByTokenIdAndSerialNumber(_, _)
      0 * repo.save(_)
      0 * queue.remove(_)
      0 * queue.unlock(_)
  }

  def "test that nfts are just removed from queue if there is a record already"() {
    given: "the following nfts are queued"
      def nfts = [new Nft("0.0.1", 1), new Nft("0.0.1", 2)]
      queue.list() >> nfts
    when:
      job.run()
    then: "the nfts are locked"
      nfts.size() * queue.lock(_)
    and: "the existing records are looked up"
      nfts.size() * repo.findByTokenIdAndSerialNumber(_, _) >> new EsgOffset()
    and: "save is not called"
      0 * repo.save(_)
    and: "the nfts are removed from the queue"
      nfts.size() * queue.remove(_)
  }

  def "test that new data is saved when offset data does not already exist"() {
    given: "the following nfts are queued"
      def nfts = [new Nft("0.0.1", 1), new Nft("0.0.1", 2)]
      queue.list() >> nfts
    when:
      job.run()
    then: "all nfts are locked"
      nfts.size() * queue.lock(_)
    and: "records are not found"
      nfts.size() * repo.findByTokenIdAndSerialNumber(_, _) >> null
    and: "attributes are returned from the client"
      nfts.size() * client.getNftAttributes(_, _) >> { tokenId, serialNumber ->
        new EsgAttributes(tokenId, serialNumber, [])
      }
    and: "all tokens save"
      nfts.size() * repo.save(_)
    and: "all tokens are removed from the queue"
      nfts.size() * queue.remove(_)
  }

  def "test that unlock is called when something goes wrong"() {
    given: "the following tokens are queued"
      def nfts = [new Nft("0.0.1", 1), new Nft("0.0.1", 2)]
      queue.list() >> nfts
    and: "first token is successful"
      repo.findByTokenIdAndSerialNumber(nfts[0].tokenId(), nfts[0].serialNumber()) >> new EsgOffset()
    and: "second token is not"
      repo.findByTokenIdAndSerialNumber(nfts[1].tokenId(), nfts[1].serialNumber()) >> {
        throw new Exception("something went wrong")
      }

    when:
      job.run()

    then: "all tokens are locked"
      nfts.size() * queue.lock(_)
    and: "the successful one is removed"
      1 * queue.remove(nfts[0])
    and: "the unsuccessful one is unlocked to try again"
      1 * queue.unlock(nfts[1])
  }
}
