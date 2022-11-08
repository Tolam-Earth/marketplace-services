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

package com.tolamearth.marketplace.common;

public enum ListingOrder {
  ASC("gt:", "lt:"), DESC("lt:", "gt:");

  private final String nextPrefix;
  private final String previousPrefix;

  ListingOrder(String nextPrefix, String previousPrefix) {
    this.nextPrefix = nextPrefix;
    this.previousPrefix = previousPrefix;
  }

  // TODO: decide if these names are good or come up with better ones
  // TODO: context: these are prefixes added to the tokenId to support pagination
  public String nextPrefix() {
    return nextPrefix;
  }

  public String previousPrefix() {
    return previousPrefix;
  }
}
