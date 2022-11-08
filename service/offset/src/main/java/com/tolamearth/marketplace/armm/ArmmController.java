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

package com.tolamearth.marketplace.armm;

import com.tolamearth.marketplace.common.web.ParameterValidator;
import com.tolamearth.marketplace.esg.EsgLoadQueue;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.RequestBean;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

@Controller("/hem/v1/price")
@ExecuteOn(TaskExecutors.IO)
public class ArmmController implements ParameterValidator {

  private final ArmmClient client;
  private final PriceRequestValidator validator;
  private final EsgLoadQueue esgLoadQueue;

  public ArmmController(ArmmClient client, PriceRequestValidator validator,
      EsgLoadQueue esgLoadQueue) {
    this.client = client;
    this.validator = validator;
    this.esgLoadQueue = esgLoadQueue;
  }

  @Post
  PriceResponse getPrices(@RequestBean PriceRequest request) {
    validator.validate(request);
    request.actualNfts().forEach(esgLoadQueue::add);
    return client.getPrices(request).toPriceResponse();
  }
}
