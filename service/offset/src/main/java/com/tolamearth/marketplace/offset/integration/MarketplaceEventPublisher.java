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

package com.tolamearth.marketplace.offset.integration;

import com.tolamearth.integration.marketplace.MarketplaceEvent;
import io.micronaut.gcp.pubsub.annotation.PubSubClient;
import io.micronaut.gcp.pubsub.annotation.Topic;

@PubSubClient(project = "hem-integration-services")
public interface MarketplaceEventPublisher {

  @Topic("${hem.event.marketplace.topic:pub_nft_marketplace_state}")
  String publish(byte[] listingBytes);

  default String publish(MarketplaceEvent event) {
    return publish(event.toByteArray());
  }
}