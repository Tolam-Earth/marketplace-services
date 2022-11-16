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

package com.tolamearth.marketplace.offset;

import static com.tolamearth.marketplace.offset.ListingState.LISTED;
import static com.tolamearth.marketplace.offset.ListingState.UNLISTED;

import com.tolamearth.marketplace.common.HederaAccount;
import com.tolamearth.marketplace.common.ListingOrder;
import com.tolamearth.marketplace.common.error.HemErrorCode;
import com.tolamearth.marketplace.common.error.HemException;
import com.tolamearth.marketplace.common.util.StreamHelpers;
import com.tolamearth.marketplace.mirrornode.AccountService;
import com.tolamearth.marketplace.offset.db.OffsetRepo;
import com.tolamearth.marketplace.smartcontract.TransactionService;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Singleton
public class MirrorNodeOffsetService implements OffsetService {

  private final AccountService accountService;
  private final OffsetRepo offsetRepo;
  private final TransactionService transactionService;

  public MirrorNodeOffsetService(AccountService accountService,
      OffsetRepo offsetRepo, TransactionService transactionService) {
    this.accountService = accountService;
    this.offsetRepo = offsetRepo;
    this.transactionService = transactionService;
  }

  @Override
  public List<ListingStateOffset> fetchOffsets(HederaAccount account, String tokenId, Integer limit,
      ListingOrder order, ListingState state) {

    if (!accountService.isAccountValid(account)) {
      throw new HemException(HemErrorCode.UNKNOWN_RESOURCE);
    }

    if (state == LISTED) {
      return fetchListed(account, tokenId, limit, order);
    } else if (state == UNLISTED) {
      return fetchUnlisted(account, tokenId, limit, order);
    }

    var listed = fetchListed(account, tokenId, limit, order);
    var unlisted = fetchUnlisted(account, tokenId, limit, order).stream()
        .filter(offset -> listed.stream().noneMatch(listedOffset -> offset.offset().nft().tokenId().equals(listedOffset.offset().nft().tokenId()) && offset.offset().nft().serialNumber().equals(listedOffset.offset().nft().serialNumber()))).toList();
    var combined = Stream.of(listed, unlisted)
        .flatMap(Collection::stream)
        .sorted();
    if (order == ListingOrder.DESC) {
      combined = StreamHelpers.reverse(combined);
    }
    return combined
        .limit(limit)
        .toList();
  }

  private List<ListingStateOffset> fetchUnlisted(HederaAccount account, String tokenId,
      Integer limit, ListingOrder order) {
    List<SimpleOffset> offsets = accountService.fetchOffsets(account, tokenId, limit, order);
    if (offsets == null || offsets.isEmpty()) {
      return new ArrayList<>();
    }

    return offsets.stream().map(offset -> new ListingStateOffset(UNLISTED, offset)).toList();
  }

  private List<ListingStateOffset> fetchListed(HederaAccount account, String tokenId, Integer limit,
      ListingOrder order) {
    var offsets = offsetRepo.findCurrentlyListedByOwnerId(account.getId());
    if (offsets == null || offsets.isEmpty()) {
      return new ArrayList<>();
    }
    var offsetStream = offsets.stream();
    if (order == ListingOrder.DESC) {
      // reverse stream
      offsetStream = StreamHelpers.reverse(offsetStream);
      if (tokenId != null) {
        offsetStream = offsetStream.filter(offset -> offset.nft().tokenId().compareTo(tokenId) < 0);
      }
    } else {
      if (tokenId != null) {
        offsetStream = offsetStream.filter(offset -> offset.nft().tokenId().compareTo(tokenId) > 0);
      }
    }
    return offsetStream
        .map(offset -> new ListingStateOffset(LISTED, new SimpleOffset(offset)))
        .limit(limit)
        .toList();
  }

  @Override
  public List<ListingStateOffset> fetchOffsets(String tokenId, Integer limit,
      ListingOrder order, ListingState state) {
    return offsetRepo.findAllByTransactionState(ListingTransactionState.LISTED).stream()
        .map(offset -> new ListingStateOffset(LISTED,
            new SimpleOffset(offset.ownerId(), offset.nft(), offset.price())))
        .toList();

  }

  @Override
  public void list(String accountId, String transactionId, List<PricedNft> pricedNfts) {
    if (pricedNfts.stream()
        .map(PricedNft::withoutPrice)
        .map(offsetRepo::findByNft)
        .anyMatch(it -> it.state() == LISTED)) {
      throw new HemException(HemErrorCode.ALREADY_IN_PROGRESS);
    }
    offsetRepo.addListing(accountId, transactionId, pricedNfts);
    transactionService.allowList(accountId, transactionId, pricedNfts);
    offsetRepo.approveListing(transactionId);
  }

  @Override
  public void purchase(String accountId, String transactionId, List<Nft> nfts) {
    if (nfts.stream()
        .map(offsetRepo::findByNft)
        .anyMatch(it -> it.state() == UNLISTED)) {
      throw new HemException(HemErrorCode.ALREADY_IN_PROGRESS);
    }
    if(!accountService.isAccountValid(new HederaAccount(accountId))){
      throw new HemException(HemErrorCode.UNKNOWN_RESOURCE);
    }
    offsetRepo.updateListings(transactionId, nfts);// update the listing
    transactionService.purchaseList(accountId, transactionId, nfts);// make the purchase
    offsetRepo.updatePurchasedTransactionState(transactionId,
        ListingTransactionState.APPROVED);// store the purchase transaction
  }
}
