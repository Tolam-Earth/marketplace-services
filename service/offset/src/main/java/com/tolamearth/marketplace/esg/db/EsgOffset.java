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

package com.tolamearth.marketplace.esg.db;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.annotation.Relation.Cascade;
import io.micronaut.data.annotation.Relation.Kind;
import java.util.List;

@MappedEntity("EsgOffset")
public class EsgOffset {

  @Id
  @GeneratedValue
  private Long id;

  private String tokenId;
  private Long serialNumber;
  @Relation(value=Kind.ONE_TO_MANY, mappedBy = "esgOffset", cascade = Cascade.ALL)
  private List<EsgOffsetAttribute> attributes;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTokenId() {
    return tokenId;
  }

  public void setTokenId(String tokenId) {
    this.tokenId = tokenId;
  }

  public Long getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(Long serialNumber) {
    this.serialNumber = serialNumber;
  }

  public List<EsgOffsetAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<EsgOffsetAttribute> attributes) {
    this.attributes = attributes;
  }

  public String getAttribute(String title) {
    return attributes.stream()
        .filter(attribute -> attribute.getTitle().equals(title))
        .map(EsgOffsetAttribute::getValue)
        .findFirst().orElse(null);
  }

  public String getAttribute(KnownAttribute attribute){
    return getAttribute(attribute.title());
  }

}
