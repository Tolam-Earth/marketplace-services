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

import io.micronaut.context.annotation.Executable;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface ListingRepo extends CrudRepository<Listing, Long> {

  List<Listing> findByAccountIdAndPurchaseTxnIdIsNull(String accountId);
  List<Listing> findByTokenIdIn(List<String> tokenIds);
  List<Listing> findByTokenIdAndSerialNumber(String tokenId, Long serialNumber);
  List<Listing> findByAccountIdAndTokenIdAndSerialNumber(String accountId, String tokenId, Long serialNumber);
  List<Listing> findByTxnId(String txnId);
  List<Listing> findByPurchaseTxnId(String purchaseTxnId);
  List<Listing> findByTxnIdIn(List<String> txnIds);
  List<Listing> findByAccountIdAndTxnId(String accountId, String txnId);
  @Executable
  Integer deleteByTxnIdIn(List<String> transactionIds);
}