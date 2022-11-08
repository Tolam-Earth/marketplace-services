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

package com.tolamearth.marketplace.esg.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tolamearth.marketplace.esg.EsgClient;
import com.tolamearth.marketplace.esg.EsgLoadQueue;
import com.tolamearth.marketplace.esg.db.EsgOffset;
import com.tolamearth.marketplace.esg.db.EsgOffsetAttribute;
import com.tolamearth.marketplace.esg.db.EsgOffsetRepo;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EsgLoadJob {

  private static final Logger log = LoggerFactory.getLogger(EsgLoadJob.class);

  private final EsgClient client;
  private final EsgLoadQueue queue;
  private final EsgOffsetRepo attributesRepo;
  private final ObjectMapper objectMapper;

  public EsgLoadJob(EsgClient client, EsgLoadQueue queue, EsgOffsetRepo attributesRepo,
      ObjectMapper objectMapper) {
    this.client = client;
    this.queue = queue;
    this.attributesRepo = attributesRepo;
    this.objectMapper = objectMapper;
  }

  @Scheduled(fixedDelay = "1m")
  public void run() {
    var tokenIds = queue.list();
    // lock all first so if job runs again they don't get processed twice
    tokenIds.forEach(queue::lock);

    tokenIds.forEach(nft -> {
      log.debug("processing " + nft);
      try {
        // check if attributes already loaded
        if (attributesRepo.findByTokenIdAndSerialNumber(nft.tokenId(), nft.serialNumber())
            == null) {
          var offset = new EsgOffset();
          // populate fields
          offset.setTokenId(nft.tokenId());
          offset.setSerialNumber(nft.serialNumber());
          addAttributes(offset);
          attributesRepo.save(offset);
        } else {
          log.debug("Tried processing already populated token: " + nft);
        }

        queue.remove(nft);
      } catch (Exception e) {
        e.printStackTrace();
        queue.unlock(nft); // try again on the next run
      }
    });
  }

  private void addAttributes(EsgOffset offset) {

    var attributes = client.getNftAttributes(offset.getTokenId(), offset.getSerialNumber())
        .attributes();
    var convertedAttributes = new ArrayList<EsgOffsetAttribute>();
    attributes.forEach(attribute -> {
      var title = (String) attribute.get("title");
      if (convertedAttributes.stream()
          .noneMatch(convertedAttribute -> convertedAttribute.getTitle().equals(title))) {
        var attributeEntity = new EsgOffsetAttribute();
        attributeEntity.setTitle(title);
        attributeEntity.setDescription(attribute.get("description").toString());
        attributeEntity.setType(attribute.get("type").toString());
        String value;
        Object unconvertedValue = attribute.get("value");
        if (attributeEntity.getType().equals("array")) {
          try {
            value = objectMapper.writeValueAsString(unconvertedValue);
          } catch (JsonProcessingException e) {
            log.error("Value on array type attribute did not properly convert to json, value toString: " + unconvertedValue.toString());
            value = unconvertedValue.toString();
          }
        } else {
          value = unconvertedValue.toString();
        }
        attributeEntity.setValue(value);
        attributeEntity.setSchemaId(attribute.get("schemaId").toString());
        attributeEntity.setSchemaName(attribute.get("schemaName").toString());
        convertedAttributes.add(attributeEntity);
      } else {
        log.error("Found and ignored duplicate attribute with title: '" + title
            + "' for offset: {tokenId: " + offset.getTokenId() + ", serial: "
            + offset.getSerialNumber() + "}, attribute value: " + attribute.get("value")
            .toString());
      }
    });
    offset.setAttributes(convertedAttributes);
  }
}
