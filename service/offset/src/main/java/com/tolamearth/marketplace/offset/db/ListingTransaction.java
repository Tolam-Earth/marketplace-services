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

import com.tolamearth.marketplace.offset.ListingTransactionState;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import javax.validation.constraints.NotEmpty;

@MappedEntity("ListedTransactions")
public class ListingTransaction {

  @Id
  @GeneratedValue
  private Long id;
  private String transactionId;
  private ListingTransactionState listingTransactionState;
  private Long creationTime;
  private Long lastUpdateTime;

  public ListingTransaction(@NotEmpty String transactionId,
      @NonNull ListingTransactionState listingTransactionState, @NonNull Long creationTime,
      @NonNull Long lastUpdateTime) {

    this.transactionId = transactionId;
    this.listingTransactionState = listingTransactionState;
    this.creationTime = creationTime;
    this.lastUpdateTime = lastUpdateTime;
  }

  @Id
  public Long getId() {
    return id;
  }

  public void setId(@Id @NonNull Long id) {
    this.id = id;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public ListingTransactionState getListingTransactionState() {
    return listingTransactionState;
  }

  public void setListingTransactionState(ListingTransactionState listingTransactionState) {
    this.listingTransactionState = listingTransactionState;
  }

  public Long getCreationTime() {
    return creationTime;
  }

  public Long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public void setCreationTime(Long creationTime) {
    this.creationTime = creationTime;
  }

  public void setLastUpdateTime(Long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  @Override
  public String toString() {
    return listingTransactionState.toString();
  }
}