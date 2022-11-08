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

package com.tolamearth.marketplace.esg.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tolamearth.marketplace.armm.ArmmService;
import com.tolamearth.marketplace.common.error.HemErrorCode;
import com.tolamearth.marketplace.common.error.HemException;
import com.tolamearth.marketplace.esg.EsgLoadQueue;
import com.tolamearth.marketplace.esg.SearchResponse;
import com.tolamearth.marketplace.esg.db.EsgOffset;
import com.tolamearth.marketplace.esg.db.EsgOffsetRepo;
import com.tolamearth.marketplace.esg.db.EsgOffsetSearchRepo;
import com.tolamearth.marketplace.esg.web.EsgResponse.EsgCredentialSubject;
import com.tolamearth.marketplace.offset.Nft;
import com.tolamearth.marketplace.offset.db.OffsetRepo;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;

import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/hem/${hem.api.version}")
@ExecuteOn(TaskExecutors.IO)
public class EsgController {

  private static final Logger log = LoggerFactory.getLogger(EsgController.class);

  private final OffsetRepo offsetRepo;
  private final ArmmService armmService;
  private final EsgOffsetRepo attributesRepo;
  private final EsgLoadQueue queue;
  private final ObjectMapper objectMapper;
  private final EsgOffsetSearchRepo esgOffsetSearchRepo;

  public EsgController(OffsetRepo offsetRepo, ArmmService armmService,
      EsgOffsetRepo attributesRepo, EsgLoadQueue queue, ObjectMapper objectMapper,
      EsgOffsetSearchRepo esgOffsetSearchRepo) {
    this.offsetRepo = offsetRepo;
    this.armmService = armmService;
    this.attributesRepo = attributesRepo;
    this.queue = queue;
    this.objectMapper = objectMapper;
    this.esgOffsetSearchRepo = esgOffsetSearchRepo;
  }

  @Get("/esg")
  EsgResponse load(@RequestBean EsgRequest request) {
    var esgData = loadEsgData(request.getTokenId(), request.getSerialNumber());
    if (esgData == null) {
      throw new HemException(HemErrorCode.UNKNOWN_RESOURCE, new NullPointerException(
          "Unable to find ESG details for token id: " + request.getTokenId()));
    }
    if (esgData.getAttributes() == null) {
      esgData.setAttributes(List.of());
    }
    var armmData = armmService.getPrices(
        List.of(new Nft(request.getTokenId(), request.getSerialNumber())));
    if (armmData == null || armmData.isEmpty()) {
      throw new HemException(HemErrorCode.UNKNOWN_RESOURCE,
          new NullPointerException("Unable to fetch ARMM prices"));
    }
    var offset = offsetRepo.findByTokenIdAndSerialNumber(request.getTokenId(),
        request.getSerialNumber());
    if (offset == null) {
      throw new HemException(HemErrorCode.UNKNOWN_RESOURCE, new NullPointerException(
          "Unable to load token information, does nft actually exist? token id: "
              + request.getTokenId()));
    }
    var armmPrice = armmData.get(0);
    return new EsgResponse(request,
        offset.ownerId(),
        offset.price(),
        armmPrice.minPrice(),
        armmPrice.maxPrice(),
        esgData.getAttribute("Project Category"),
        esgData.getAttribute("Project Type"),
        esgData.getAttribute("Project Name"),
        esgData.getAttribute("Project Country"),
        esgData.getAttribute("Project Region"),
        esgData.getAttribute("Vintage"),
        convertSubjects(esgData)
    );
  }


  List<EsgCredentialSubject> convertSubjects(EsgOffset offset) {
    return offset.getAttributes().stream().map(attribute -> {
      Object value = attribute.getValue();
      if (attribute.getType().equals("array")) {
        try {
          value = objectMapper.readValue((String)value, List.class);
        } catch (JsonProcessingException e) {
          log.error("badly formed json on attribute id #" +attribute.getId()+ ", value: " + value);
        }
      }
      return new EsgCredentialSubject(attribute.getTitle(), attribute.getDescription(), value,
          attribute.getType(),
          attribute.getSchemaId(), attribute.getSchemaName());
    }).toList();
  }

  @Post("/simplesearch")
  @Status(HttpStatus.OK)
  SearchResponse search(@Body Map<String, List<String>> parameters) {
    return new SearchResponse(parameters, esgOffsetSearchRepo.findByParameters(parameters));
  }

  private EsgOffset loadEsgData(String tokenId, Long serialNumber) {
    var attributes = attributesRepo.findByTokenIdAndSerialNumber(tokenId, serialNumber);

    if (attributes == null) {
      var nft = new Nft(tokenId, serialNumber);
      if (!queue.contains(nft)) {
        queue.add(nft);
      }
    }
    return attributes;
  }
}

