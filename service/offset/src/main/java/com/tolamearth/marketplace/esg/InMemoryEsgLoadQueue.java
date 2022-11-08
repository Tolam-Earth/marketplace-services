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

package com.tolamearth.marketplace.esg;

import com.tolamearth.marketplace.offset.Nft;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Singleton
public class InMemoryEsgLoadQueue implements EsgLoadQueue {

  private final Map<Nft, Boolean> tokens;

  public InMemoryEsgLoadQueue() {
    tokens = new HashMap<>();
  }

  @Override
  public boolean contains(Nft nft) {
    return tokens.containsKey(nft);
  }

  @Override
  public boolean add(Nft nft) {
    if (tokens.containsKey(nft)) {
      return false;
    }
    tokens.put(nft, false);
    return true;
  }

  @Override
  public boolean lock(Nft nft) {
    if (tokens.containsKey(nft)) {
      if (!tokens.get(nft)) {
        tokens.remove(nft);
        tokens.put(nft, true);
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean unlock(Nft nft) {
    if (tokens.containsKey(nft)) {
      if (tokens.get(nft)) {
        tokens.remove(nft);
        tokens.put(nft, false);
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean remove(Nft nft) {
    if (tokens.containsKey(nft)) {
      tokens.remove(nft);
      return true;
    }
    return false;
  }

  @Override
  public List<Nft> list(boolean unlockedOnly) {
    return tokens.entrySet()
        .stream()
        .filter(entry -> !(unlockedOnly && entry.getValue()))
        .map(Entry::getKey)
        .toList();
  }
}
