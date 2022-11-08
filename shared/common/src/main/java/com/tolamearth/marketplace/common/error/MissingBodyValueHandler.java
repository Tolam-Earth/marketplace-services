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

package com.tolamearth.marketplace.common.error;

import com.tolamearth.marketplace.common.error.HemExceptionHandler.RenderedError;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpResponseFactory;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.web.router.exceptions.UnsatisfiedBodyRouteException;
import jakarta.inject.Singleton;
import java.util.Map;

@Singleton
@Produces
public class MissingBodyValueHandler implements ExceptionHandler<UnsatisfiedBodyRouteException, HttpResponse> {
  @Override
  public HttpResponse handle(HttpRequest request, UnsatisfiedBodyRouteException exception) {
    var errorCode = HemErrorCode.MISSING_REQUIRED_FIELD;
    RenderedError rendered = new RenderedError(errorCode);
    Map<String, RenderedError> responseModel = Map.of("error", rendered);
    return HttpResponseFactory.INSTANCE.status(HttpStatus.valueOf(errorCode.getHttpCode())).body(responseModel);
  }
}
