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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.annotation.QueryValue;
import java.util.Objects;

@Introspected
public class EsgRequest {

  @QueryValue("token_id")
  @JsonProperty("token_id")
  private String tokenId;
  @QueryValue("serial_number")
  @JsonProperty("serial_number")
  private Long serialNumber;

  public EsgRequest(){}

  public EsgRequest(String tokenId, Long serialNumber) {
    this.tokenId = tokenId;
    this.serialNumber = serialNumber;
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

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    var that = (EsgRequest) obj;
    return Objects.equals(this.tokenId, that.tokenId) &&
        Objects.equals(this.serialNumber, that.serialNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tokenId, serialNumber);
  }

  @Override
  public String toString() {
    return "EsgRequest[" +
        "tokenId=" + tokenId + ", " +
        "serialNumber=" + serialNumber + "]";
  }


}
