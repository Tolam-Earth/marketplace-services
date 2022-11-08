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
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface PurchasedTransactionRepo extends CrudRepository<PurchasedTransaction, Long> {
  List<String> findTxnIdByPurchasedStateAndLastUpdateTimeLessThan(
      ListingTransactionState purchasedState, Long lastUpdateTime);
  Integer deleteByTxnIdIn(List<String> txnIds);
  List<PurchasedTransaction> findByPurchasedStateAndLastUpdateTimeLessThan(ListingTransactionState purchasedState, long lastUpdateTime);

  Integer updateById(Long id, ListingTransactionState purchasedState);
  PurchasedTransaction getByTxnId(String transactionId);
}
