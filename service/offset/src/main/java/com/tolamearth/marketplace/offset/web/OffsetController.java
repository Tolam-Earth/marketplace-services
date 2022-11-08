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

package com.tolamearth.marketplace.offset.web;

import com.tolamearth.marketplace.common.HederaAccount;
import com.tolamearth.marketplace.common.ListingOrder;
import com.tolamearth.marketplace.common.error.HemErrorCode;
import com.tolamearth.marketplace.common.error.HemException;
import com.tolamearth.marketplace.offset.ListingState;
import com.tolamearth.marketplace.offset.ListingTransactionState;
import com.tolamearth.marketplace.offset.OffsetService;
import com.tolamearth.marketplace.offset.db.ListingTransaction;
import com.tolamearth.marketplace.offset.db.ListingTransactionRepo;
import com.tolamearth.marketplace.offset.db.PurchasedTransaction;
import com.tolamearth.marketplace.offset.db.PurchasedTransactionRepo;
import com.tolamearth.marketplace.offset.TransactionTypeCode;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Controller("/hem/v1/offsets")
@ExecuteOn(TaskExecutors.IO)
public class OffsetController {

  private static final Logger log = LoggerFactory.getLogger(OffsetController.class);

  private final OffsetService offsetService;
  private final ListingRequestValidator listingRequestValidator;
  private final PurchaseRequestValidator purchaseRequestValidator;
  private final PurchasedTransactionRepo purchasedTransactionRepo;
  private final ListingTransactionRepo listingTransactionRepo;

  public OffsetController(OffsetService offsetService,
      ListingRequestValidator listingRequestValidator,
      PurchaseRequestValidator purchaseRequestValidator,
      PurchasedTransactionRepo purchasedTransactionRepo,
      ListingTransactionRepo listingTransactionRepo) {
    this.offsetService = offsetService;
    this.listingRequestValidator = listingRequestValidator;
    this.purchaseRequestValidator = purchaseRequestValidator;
    this.purchasedTransactionRepo = purchasedTransactionRepo;
    this.listingTransactionRepo = listingTransactionRepo;
  }

  @Get
  OffsetListResponse loadOffsets(
      @QueryValue("account_id") @Nullable String accountId,
      @Nullable String tokenId,
      @Nullable Integer limit,
      @Nullable ListingOrder order,
      @QueryValue(value = "list_state", defaultValue = "all") ListingState state) {

    if (limit == null || (limit != null && limit < 0)) {
      limit = 50;
    }
    if(limit > 100){
      limit = 100;
    }
    if (order == null) {
      order = ListingOrder.ASC;
    }

    var request = new OffsetListRequest(accountId, tokenId, order, limit, state);
    try {
      HederaAccount account = new HederaAccount(accountId);
      return new OffsetListResponse(
          offsetService.fetchOffsets(account, tokenId, limit, order, state),
          request);
    } catch (NullPointerException e) {
      return new OffsetListResponse(offsetService.fetchOffsets(tokenId, limit, order, state),
          request);
    }
  }

  @Get("/all-listed")
  OffsetListResponse loadOffsets(
      @Nullable String tokenId,
      @Nullable Integer limit,
      @Nullable ListingOrder order,
      @QueryValue(value = "list_state", defaultValue = "listed") ListingState state) {
    if (limit == null) {
      limit = 25;
    } else if (limit > 100) {
      limit = 100;
    }
    if (order == null) {
      order = ListingOrder.ASC;
    }

    return new OffsetListResponse(offsetService.fetchOffsets(tokenId, limit, order, state),
        new OffsetListRequest(null, tokenId, order, limit, state));
  }

  @Post("/list")
  @Status(HttpStatus.CREATED)
  ListingResponse createListing(@NonNull @Body ListingRequest request) {
    listingRequestValidator.validate(request);
    offsetService.list(request.accountId(), request.transactionId(), request.pricedNfts());
    return new ListingResponse(request);
  }

  @Post("/purchase")
  @Status(HttpStatus.OK)
  PurchaseResponse purchaseNfts(@NonNull @Body PurchaseRequest request) {
    purchaseRequestValidator.validate(request);
    offsetService.purchase(request.accountId(), request.transactionId(), request.nfts());
    return new PurchaseResponse(request);//TODO should return HTTP response code 200 with Response status of PENDING?
  }

  @Get("/txn")
  @Status(HttpStatus.OK)
  RetrieveTransactionRecordInfoResponse retrieveTransactionRecordInfo(@QueryValue String txn_id, @QueryValue("txn_type") String txn_type) {
    TransactionTypeCode transactionTypeCode = TransactionTypeCode.valueOf(txn_type);
    ListingTransactionState transactionState = (transactionTypeCode == TransactionTypeCode.PURCHASE) ?
            findPurchasedTransactionState(txn_id) : findListedTransactionState(txn_id);
    if (transactionState == null) {
      throw new HemException(HemErrorCode.INVALID_DATA);
    }
    return new RetrieveTransactionRecordInfoResponse(Map.of("txn_id", txn_id, "txn_type", txn_type), transactionState);
  }
  private ListingTransactionState findPurchasedTransactionState(String txn_id){
    PurchasedTransaction purchasedTransaction = purchasedTransactionRepo.getByTxnId(txn_id);
    return purchasedTransaction != null ? purchasedTransaction.getPurchasedState() : null;
  }
  private ListingTransactionState findListedTransactionState(String txn_id){
    ListingTransaction listingTransaction = listingTransactionRepo.getByTransactionId(txn_id);
    return listingTransaction != null ? listingTransaction.getListingTransactionState() : null;
  }
}
