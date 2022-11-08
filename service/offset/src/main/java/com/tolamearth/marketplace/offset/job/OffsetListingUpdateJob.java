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

package com.tolamearth.marketplace.offset.job;

import com.tolamearth.marketplace.offset.integration.TransactionMessageHandler;
import com.tolamearth.marketplace.offset.job.PendingTransactions.PendingTransaction;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OffsetListingUpdateJob {

  private static final Logger log = LoggerFactory.getLogger(OffsetListingUpdateJob.class);

  private final ExpiryChecker creationExpiryChecker;
  private final ExpiryChecker approvalExpiryChecker;
  private final PurchasedExpiryChecker purchasedCreationExpiryChecker;
  private final PurchasedExpiryChecker purchasedApprovalExpiryChecker;
  private final PendingTransactions pendingTransactions;
  private final TransactionValidator transactionValidator;
  private final TransactionMessageHandler transactionMessageHandler;

  public OffsetListingUpdateJob(@Named("creation") ExpiryChecker creationExpiryChecker,
      @Named("approval") ExpiryChecker approvalExpiryChecker,
      @Named("purchasedCreation") PurchasedExpiryChecker purchasedCreationExpiryChecker,
      @Named("purchasedApproval") PurchasedExpiryChecker purchasedApprovalExpiryChecker,
      PendingTransactions pendingTransactions,
      TransactionValidator transactionValidator,
      TransactionMessageHandler transactionMessageHandler) {
    this.creationExpiryChecker = creationExpiryChecker;
    this.approvalExpiryChecker = approvalExpiryChecker;
    this.purchasedCreationExpiryChecker = purchasedCreationExpiryChecker;
    this.purchasedApprovalExpiryChecker = purchasedApprovalExpiryChecker;
    this.pendingTransactions = pendingTransactions;
    this.transactionValidator = transactionValidator;
    this.transactionMessageHandler = transactionMessageHandler;
  }

  @Scheduled(fixedDelay = "5s")
  public void run() {
    // find and remove expired create
    int removedCreate = creationExpiryChecker.run();
    log.info("Removed " + removedCreate + " expired CREATE listings");

    // find and remove expired approved
    int removedApproved = approvalExpiryChecker.run();
    log.info("Removed " + removedApproved + " expired APPROVED listings");

    // find and remove expired purchased create
    int removedPurchasedCreate = purchasedCreationExpiryChecker.run();
    log.info("Removed " + removedPurchasedCreate + " expired purchased CREATE listings");

    // find and remove expired purchased approved
    int removedPurchasedApproved = purchasedApprovalExpiryChecker.run();
    log.info("Removed " + removedPurchasedApproved + " expired purchased APPROVED listings");

    // find and update valid listings
    AtomicInteger updatedListings = new AtomicInteger();
    List<PendingTransaction> validated = new ArrayList<>();
    pendingTransactions.list().forEach(pendingTransaction -> {
      var success = transactionValidator.validate(pendingTransaction);
      if (success) {
        updatedListings.getAndIncrement();
        validated.add(pendingTransaction);
      }
    });
    log.info("Updated " + updatedListings + " valid approved listings");

    transactionMessageHandler.publish(validated);
  }
}
