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

package com.tolamearth.marketplace.esg.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EsgResponse(EsgRequest request,
                          @JsonProperty("actual_owner") String actualOwnerId,
                          @JsonProperty("retail_price") Long retailPrice,
                          @JsonProperty("min_arm_price") Long minArmPrice,
                          @JsonProperty("max_arm_price") Long maxArmPrice,
                          @JsonProperty("project_category") String projectCategory,
                          @JsonProperty("project_type") String projectType,
                          @JsonProperty("project_name") String projectName,
                          @JsonProperty("project_country") String projectCountry,
                          @JsonProperty("project_region") String projectRegion,
                          String vintage,
                          @JsonProperty("credential_subjects") List<EsgCredentialSubject> credentialSubjects
) {


  record EsgCredentialSubject(String title, String description, Object value, String type,
                              @JsonProperty("schema_id") String schemaId,
                              @JsonProperty("schema_name") String schemaName) {

  }

}
