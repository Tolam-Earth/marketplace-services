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

import static com.tolamearth.marketplace.offset.ListingState.LISTED;

import com.tolamearth.marketplace.common.error.HemErrorCode;
import com.tolamearth.marketplace.common.error.HemException;
import com.tolamearth.marketplace.mirrornode.TokenClient;
import com.tolamearth.marketplace.offset.ListingState;
import com.tolamearth.marketplace.offset.ListingTransactionState;
import com.tolamearth.marketplace.offset.Nft;
import com.tolamearth.marketplace.offset.Offset;
import com.tolamearth.marketplace.offset.OffsetListing;
import com.tolamearth.marketplace.offset.PricedNft;
import jakarta.inject.Singleton;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;

@Singleton
public class OffsetRepo {

  private final ListingTransactionRepo listingTransactionRepo;
  private final PurchasedTransactionRepo purchasedTransactionRepo;
  private final ListingRepo listingRepo;
  private final TokenClient tokenClient;
  private final Clock clock;

  public OffsetRepo(ListingTransactionRepo listingTransactionRepo,
      PurchasedTransactionRepo purchasedTransactionRepo,
      ListingRepo listingRepo, TokenClient tokenClient, Clock clock) {

    this.listingTransactionRepo = listingTransactionRepo;
    this.purchasedTransactionRepo = purchasedTransactionRepo;
    this.listingRepo = listingRepo;
    this.tokenClient = tokenClient;
    this.clock = clock;
  }

  public List<Offset> findAllByListingTransactionId(String transactionId) {
    return findAllByListingTransactionId(transactionId, true);
  }

  public List<Offset> findAllByListingTransactionId(String transactionId, boolean currentOnly) {
    var listings = listingRepo.findByTxnId(transactionId); // maybe change this to a projection
    return listings.stream().filter(listing -> listing.getPurchaseTxnId() == null || !currentOnly)
        .map(
            listing -> findByTokenIdAndSerialNumber(listing.getTokenId(), listing.getSerialNumber())
        ).toList();
  }

  public List<Offset> findAllByPurchaseTransactionId(String transactionId) {
    var listings = listingRepo.findByPurchaseTxnId(transactionId);
    return listings.stream()
        .map(
            listing -> findByTokenIdAndSerialNumber(listing.getTokenId(), listing.getSerialNumber())
        ).toList();
  }

  public Offset findByNft(Nft nft) {
    return findByTokenIdAndSerialNumber(nft.tokenId(), nft.serialNumber());
  }

  public Offset findByTokenIdAndSerialNumber(String tokenId, Long serialNumber) {
    var listings = listingRepo.findByTokenIdAndSerialNumber(tokenId, serialNumber);
    var possibleCurrentListing = listings.stream().filter(it -> it.getPurchaseTxnId() == null)
        .findFirst();
    if (possibleCurrentListing.isPresent()) {
      var currentListing = possibleCurrentListing.get();

      return new Offset(currentListing.getAccountId(), new Nft(tokenId, serialNumber),
          currentListing.getRetailPrice(), ListingState.LISTED,
          listings.stream().map(this::mapListingData).toList()
      );
    } else { // no active listing
      var tokenNft = tokenClient.getNft(tokenId, serialNumber);
      return new Offset(tokenNft.accountId(),
          new Nft(tokenId, serialNumber),
          null,
          ListingState.UNLISTED,
          listings.stream().map(this::mapListingData).toList()
      );
    }
  }

  public List<Offset> findCurrentlyListedByOwnerId(String ownerId) {
    var listings = listingRepo.findByAccountIdAndPurchaseTxnIdIsNull(ownerId);
    return listings.stream().sorted(Comparator.comparing(Listing::getTokenId))
        .map(listing -> new Offset(listing.getAccountId(),
            new Nft(listing.getTokenId(), listing.getSerialNumber()),
            listing.getRetailPrice(),
            LISTED,
            List.of(mapListingData(listing)))
        ).toList();
  }

  public List<Offset> findAllByTransactionState(ListingTransactionState state) {
    List<String> transactionIds = listingTransactionRepo
        .findByListingTransactionState(state).stream()
        .map(ListingTransaction::getTransactionId)
        .toList();

    return listingRepo.findByTxnIdIn(transactionIds).stream()
        .filter(listing -> listing.getPurchaseTxnId() == null)
        .map(listing -> new Offset(listing.getAccountId(),
                new Nft(listing.getTokenId(), listing.getSerialNumber()),
                listing.getRetailPrice(),
                LISTED,
                List.of(mapListingData(listing))
            )
        ).toList();
  }

  private OffsetListing mapListingData(Listing listing) {
    var listTransaction = listingTransactionRepo.getByTransactionId(listing.getTxnId());
    return new OffsetListing(listing.getId(), listing.getTokenId(), listing.getSerialNumber(),
        listing.getAccountId(), listing.getTxnId(),
        listing.getRetailPrice(), listing.getRetailPriceTimestamp(), listing.getPurchaseTxnId(),
        listTransaction.getListingTransactionState(), listTransaction.getCreationTime(),
        listTransaction.getLastUpdateTime());
  }

  public void addListing(String accountId, String transactionId, List<PricedNft> pricedNfts) {
    var now = clock.millis();
    for (PricedNft pricedNft : pricedNfts) {
      listingRepo.save(new Listing(pricedNft.tokenId(), pricedNft.serialNumber(), accountId,
          transactionId, pricedNft.price(), now, null));
    }
    var listingTransactionDto = new ListingTransaction(
        transactionId, ListingTransactionState.CREATED, now, now);

    listingTransactionRepo.save(listingTransactionDto);
  }

  public void updateListings(String accountId, String transactionId, List<Nft> nfts) {
    nfts.forEach(nft -> {
      var listings = listingRepo.findByAccountIdAndTokenIdAndSerialNumber(accountId, nft.tokenId(), nft.serialNumber());
      if (listings.size() == 0) {
        throw new HemException(HemErrorCode.UNKNOWN_RESOURCE);
      }
      listings.stream().filter(listing -> listing.getPurchaseTxnId() == null).findFirst()
          .ifPresent(listing -> {
            listing.setPurchaseTxnId(transactionId);
            listingRepo.update(listing);
          });
    });

    var now = clock.millis();//Insert transaction info in the Purchased Offsets Transaction Table to CREATED state.
    // TODO: What is the wlTxnId value supposed to be? Is it the transaction ID of the listing?
    purchasedTransactionRepo.save(
        new PurchasedTransaction(transactionId, "XXXXXXX", ListingTransactionState.CREATED, now,
            now));
  }

  public void updatePurchasedTransactionState(String transactionId,
      ListingTransactionState listingTransactionState) {
    PurchasedTransaction purchasedTransaction = purchasedTransactionRepo.getByTxnId(transactionId);
    purchasedTransaction.setPurchasedState(listingTransactionState);
    purchasedTransaction.setLastUpdateTime(clock.millis());
    purchasedTransactionRepo.update(purchasedTransaction);
  }

  public List<OffsetListing> findListingByTokenIdIn(List<String> tokenIds) {
    return listingRepo.findByTokenIdIn(tokenIds).stream().map(this::mapListingData).toList();
  }

  public void approveListing(String transactionId) {
    var entity = listingTransactionRepo.getByTransactionId(transactionId);
    entity.setListingTransactionState(ListingTransactionState.APPROVED);
    entity.setLastUpdateTime(clock.millis());
    listingTransactionRepo.update(entity);
  }

}

