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

package com.tolamearth.marketplace.offset.db;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import java.util.Objects;
import javax.validation.constraints.NotEmpty;

@MappedEntity("ListedOffsets")
public final class Listing {

  @Id
  @GeneratedValue
  private Long id;
  private String tokenId;
  private Long serialNumber;
  private String accountId;
  private String txnId;
  private Long retailPrice;
  private Long retailPriceTimestamp;
  private String purchaseTxnId;

  public Listing(@NotEmpty String tokenId,
      @NonNull Long serialNumber,
      @NotEmpty String accountId,
      @NotEmpty String txnId,
      @Nullable Long retailPrice,
      @Nullable Long retailPriceTimestamp, // unix timestamp
      @Nullable String purchaseTxnId
  ) {
    this.tokenId = tokenId;
    this.serialNumber = serialNumber;
    this.accountId = accountId;
    this.txnId = txnId;
    this.retailPrice = retailPrice;
    this.retailPriceTimestamp = retailPriceTimestamp;
    this.purchaseTxnId = purchaseTxnId;
  }

  @Id
  public Long getId() {
    return id;
  }

  public void setId(@Id @NonNull Long id) {
    this.id = id;
  }

  @NotEmpty
  public String getTokenId() {
    return tokenId;
  }

  @NonNull
  public Long getSerialNumber() {
    return serialNumber;
  }

  @NotEmpty
  public String getAccountId() {
    return accountId;
  }

  @NotEmpty
  public String getTxnId() {
    return txnId;
  }

  @Nullable
  public Long getRetailPrice() {
    return retailPrice;
  }

  @Nullable
  public Long getRetailPriceTimestamp() {
    return retailPriceTimestamp;
  }

  @Nullable
  public String getPurchaseTxnId() {
    return purchaseTxnId;
  }

  public void setTokenId(@NotEmpty String tokenId) {
    this.tokenId = tokenId;
  }

  public void setSerialNumber(@NonNull Long serialNumber) {
    this.serialNumber = serialNumber;
  }

  public void setAccountId(@NotEmpty String accountId) {
    this.accountId = accountId;
  }

  public void setTxnId(@NotEmpty String txnId) {
    this.txnId = txnId;
  }

  public void setRetailPrice(Long retailPrice) {
    this.retailPrice = retailPrice;
  }

  public void setRetailPriceTimestamp(Long retailPriceTimestamp) {
    this.retailPriceTimestamp = retailPriceTimestamp;
  }

  public void setPurchaseTxnId(String purchaseTxnId) {
    this.purchaseTxnId = purchaseTxnId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    Listing that = (Listing) obj;
    return Objects.equals(this.tokenId, that.tokenId) &&
        Objects.equals(this.serialNumber, that.serialNumber) &&
        Objects.equals(this.accountId, that.accountId) &&
        Objects.equals(this.txnId, that.txnId) &&
        Objects.equals(this.retailPrice, that.retailPrice) &&
        Objects.equals(this.retailPriceTimestamp, that.retailPriceTimestamp) &&
        Objects.equals(this.purchaseTxnId, that.purchaseTxnId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tokenId, serialNumber, accountId, txnId, retailPrice, retailPriceTimestamp,
        purchaseTxnId);
  }

  @Override
  public String toString() {
    return "Listing[" +
        "tokenId=" + tokenId + ", " +
        "serialNumber=" + serialNumber + ", " +
        "accountId=" + accountId + ", " +
        "txnId=" + txnId + ", " +
        "retailPrice=" + retailPrice + ", " +
        "retailPriceTimestamp=" + retailPriceTimestamp +
        "purchaseTxnId=" + purchaseTxnId + ']';
  }


}