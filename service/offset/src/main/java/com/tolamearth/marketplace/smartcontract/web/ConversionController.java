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

package com.tolamearth.marketplace.smartcontract.web;

import com.tolamearth.marketplace.smartcontract.ConversionService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.math.BigInteger;

@Controller("/hem/v1/conversion")
@ExecuteOn(TaskExecutors.IO)
public class ConversionController {

  private final ConversionService conversionService;

  public ConversionController(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  record ConversionRateResponse(BigInteger rate) {

  }

  @Get
  ConversionRateResponse getConversionRate(@QueryValue(defaultValue = "TinybarToCents") String type) {
    return new ConversionRateResponse(conversionService.getTinybarPerCent());
  }
}
