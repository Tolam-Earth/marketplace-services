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


import com.tolamearth.marketplace.offset.ListingTransactionState;

/**
 * Checks for listings that have expired and removes them from the database
 *
 * Each implementation should check at most one ListingTransactionState.
 *
 * @author Jesse Elliott
 */
public interface ExpiryChecker {

  /**
   * Runs check for listings
   *
   * @return the number of records deleted
   */
  int run();

  /**
   *
   * {@return the ListingTransactionState that this will check}
   */
  ListingTransactionState getState();
}
