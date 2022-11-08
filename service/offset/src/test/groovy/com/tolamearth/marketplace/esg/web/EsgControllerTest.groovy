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

package com.tolamearth.marketplace.esg.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.tolamearth.marketplace.esg.db.EsgOffsetSearchRepo

import static com.tolamearth.marketplace.common.CustomAssertions.assertContains
import static com.tolamearth.marketplace.esg.db.KnownAttribute.PROJECT_CATEGORY
import static com.tolamearth.marketplace.esg.db.KnownAttribute.PROJECT_COUNTRY
import static com.tolamearth.marketplace.esg.db.KnownAttribute.PROJECT_NAME
import static com.tolamearth.marketplace.esg.db.KnownAttribute.PROJECT_REGION
import static com.tolamearth.marketplace.esg.db.KnownAttribute.PROJECT_TYPE
import static com.tolamearth.marketplace.util.AssertionHelper.assertions
import static org.junit.jupiter.api.Assertions.assertAll
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

import com.tolamearth.marketplace.common.error.HemException
import com.tolamearth.marketplace.esg.EsgLoadQueue
import com.tolamearth.marketplace.esg.db.EsgOffsetRepo
import com.tolamearth.marketplace.esg.db.KnownAttribute
import com.tolamearth.marketplace.offset.Nft
import com.tolamearth.marketplace.offset.Offset
import com.tolamearth.marketplace.armm.ArmmService
import com.tolamearth.marketplace.armm.NftPrice
import com.tolamearth.marketplace.esg.db.EsgOffset
import com.tolamearth.marketplace.esg.db.EsgOffsetAttribute
import com.tolamearth.marketplace.offset.db.OffsetRepo
import org.junit.jupiter.api.Test

class EsgControllerTest {

  private static final String TOKEN_ID = "0.0.1"
  private static final Long SERIAL_NUMBER = 1;
  private static final Nft NFT = new Nft(TOKEN_ID, SERIAL_NUMBER)

  private final OffsetRepo offsetRepo = mock(OffsetRepo)
  private final EsgOffsetRepo attributesRepo = mock(EsgOffsetRepo)
  private final ArmmService armmService = mock(ArmmService)
  private final EsgLoadQueue queue = mock(EsgLoadQueue)
  private final EsgOffsetSearchRepo esgOffsetSearchRepo = mock(EsgOffsetSearchRepo)
  private final ObjectMapper objectMapper = mock(ObjectMapper)
  private final EsgController controller = new EsgController(offsetRepo, armmService, attributesRepo, queue, objectMapper, esgOffsetSearchRepo)

  @Test
  void "test that an exception is thrown when esg record does not exist"() {
    when(attributesRepo.findByTokenIdAndSerialNumber(TOKEN_ID, SERIAL_NUMBER)).thenReturn(null)

    def exception = assertThrows(HemException,
        () -> controller.load(new EsgRequest(TOKEN_ID, SERIAL_NUMBER)))
    assertContains("ESG details", exception.message)
  }

  @Test
  void "test that a token id not in the queue is added to the queue"() {
    when(attributesRepo.findByTokenIdAndSerialNumber(TOKEN_ID, SERIAL_NUMBER)).thenReturn(null)

    def exception = assertThrows(HemException,
        () -> controller.load(new EsgRequest(TOKEN_ID, SERIAL_NUMBER)))

    verify(queue).add(eq(NFT))
  }

  @Test
  void "test that a token id in the queue is not added to the queue again"() {
    when(attributesRepo.findByTokenIdAndSerialNumber(TOKEN_ID, SERIAL_NUMBER)).thenReturn(null)
    when(queue.contains(eq(NFT))).thenReturn(true)

    def exception = assertThrows(HemException,
        () -> controller.load(new EsgRequest(TOKEN_ID, SERIAL_NUMBER)))

    verify(queue, times(0)).add(eq(NFT))
  }

  @Test
  void "test that an exception is thrown when armm data does not return"() {
    when(attributesRepo.findByTokenIdAndSerialNumber(TOKEN_ID, SERIAL_NUMBER)).thenReturn([:] as EsgOffset)
    when(armmService.getPrices(any())).thenReturn([])
    def exception = assertThrows(HemException,
        () -> controller.load(new EsgRequest(TOKEN_ID, SERIAL_NUMBER)))
    assertContains("ARMM", exception.message)
  }

  @Test
  void "test that an exception is thrown when offset data does not return"() {
    when(attributesRepo.findByTokenIdAndSerialNumber(TOKEN_ID, SERIAL_NUMBER)).thenReturn([:] as EsgOffset)
    when(armmService.getPrices(any())).thenReturn([new NftPrice(NFT, 5, 10, null, null)])
    when(offsetRepo.findByTokenIdAndSerialNumber(eq(TOKEN_ID), eq(1))).thenReturn(null)

    def exception = assertThrows(HemException,
        () -> controller.load(new EsgRequest(TOKEN_ID, 1)))
    assertContains("nft", exception.message)
  }

  @Test
  void "test that a token id with all information will return correctly"() {
    def attributes = new EsgOffset(tokenId: TOKEN_ID,
        serialNumber: SERIAL_NUMBER,
        attributes: [
            makeAttribute(PROJECT_CATEGORY, "Clean Generation"),
            makeAttribute(PROJECT_COUNTRY, "USA"),
            makeAttribute(PROJECT_NAME, ""),
            makeAttribute(PROJECT_REGION, "Ohio"),
            makeAttribute(PROJECT_TYPE, "Solar")
        ]
    )
    def nftPrice = new NftPrice(NFT, 5, 10, null, null)
    def nft = new Nft(TOKEN_ID, 1)
    def offset = new Offset("0.0.5", nft, 5, null, null)

    when(attributesRepo.findByTokenIdAndSerialNumber(TOKEN_ID, SERIAL_NUMBER)).thenReturn(attributes)
    when(armmService.getPrices([NFT])).thenReturn([nftPrice])
    when(offsetRepo.findByTokenIdAndSerialNumber(TOKEN_ID, 1)).thenReturn(offset)

    def request = new EsgRequest(TOKEN_ID, 1)
    def result = controller.load(request)

    def assertAttribute = {KnownAttribute attribute, String value ->
      return () -> assertEquals(attributes.getAttribute(attribute), value)
    }

    assertAll("Everything binds",
        assertions([
            () -> assertEquals(request, result.request()),
            () -> assertEquals(offset.ownerId(), result.actualOwnerId()),
            () -> assertEquals(offset.price(), result.retailPrice()),
            () -> assertEquals(nftPrice.minPrice(), result.minArmPrice()),
            () -> assertEquals(nftPrice.maxPrice(), result.maxArmPrice()),
            assertAttribute(PROJECT_CATEGORY, result.projectCategory()),
            assertAttribute(PROJECT_COUNTRY, result.projectCountry()),
            assertAttribute(PROJECT_NAME, result.projectName()),
            assertAttribute(PROJECT_REGION, result.projectRegion()),
            assertAttribute(PROJECT_TYPE, result.projectType())
        ])
    )

  }

  private static EsgOffsetAttribute makeAttribute(KnownAttribute knownAttribute, String value) {
    def attribute = new EsgOffsetAttribute();
    attribute.setTitle(knownAttribute.title());
    attribute.setDescription(knownAttribute.title());
    attribute.setValue(value);
    attribute.setType("string");
    attribute.setSchemaId("blah");
    attribute.setSchemaName("bloop");
    return attribute;
  }
}
