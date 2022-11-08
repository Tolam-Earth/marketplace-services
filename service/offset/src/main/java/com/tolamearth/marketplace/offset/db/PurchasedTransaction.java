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

@MappedEntity("PurchasedTransactions")
public class PurchasedTransaction {
    @Id
    @GeneratedValue
    private Long id;
    private String txnId;
    @NonNull
    private String wlTxnId;
    private ListingTransactionState purchasedState;
    private Long creationTime;
    private Long lastUpdateTime;

    public PurchasedTransaction(@NotEmpty String txnId,
                              @NotEmpty String wlTxnId,
                              @NonNull ListingTransactionState purchasedState, @NonNull Long creationTime,
                              @NonNull Long lastUpdateTime) {

        this.txnId = txnId;
        this.wlTxnId = wlTxnId;
        this.purchasedState = purchasedState;
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

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    @NonNull
    public String getWlTxnId() {
        return wlTxnId;
    }

    public ListingTransactionState getPurchasedState() {
        return purchasedState;
    }

    public void setPurchasedState(ListingTransactionState purchasedState) {
        this.purchasedState = purchasedState;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        return purchasedState.toString();
    }
}
