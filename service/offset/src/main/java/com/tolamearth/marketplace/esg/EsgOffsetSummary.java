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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tolamearth.marketplace.offset.Nft;

public record EsgOffsetSummary(@JsonProperty("owner_id") String ownerId,
                               @JsonProperty("nft") Nft nft,
                               @JsonProperty("price") Long price,
                               @JsonProperty("project_category") String projectCategory,
                               @JsonProperty("project_type") String projectType,
                               @JsonProperty("project_name") String projectName,
                               @JsonProperty("project_country") String projectCountry,
                               @JsonProperty("project_region") String projectRegion,
                               @JsonProperty("vintage") String vintage){
}