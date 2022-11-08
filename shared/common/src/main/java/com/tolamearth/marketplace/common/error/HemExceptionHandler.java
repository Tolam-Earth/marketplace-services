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

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpResponseFactory;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces
@Singleton
@Requires(classes = {HemException.class, ExceptionHandler.class})
public class HemExceptionHandler implements ExceptionHandler<HemException, HttpResponse<Map<String, HemExceptionHandler.RenderedError>>> {

   private static final Logger log = LoggerFactory.getLogger(HemExceptionHandler.class);

    @Override
    public HttpResponse<Map<String, RenderedError>> handle(HttpRequest request, HemException exception) {
        if (exception.getCause() != null) {
            log.info("Error handled, code: " + exception.getCode().getCode() + ", cause message is: " + exception.getCause().getMessage());
        } else {
            log.info("Error handled, no cause provided. Code is " + exception.getCode().getMessage());
        }
        HemErrorCode errorCode = exception.getCode();

        Map<String, RenderedError> responseModel = Map.of("error", new RenderedError(errorCode));

        return HttpResponseFactory.INSTANCE.status(HttpStatus.valueOf(errorCode.getHttpCode())).body(responseModel);
    }

    record RenderedError(String message, Integer code) {

        RenderedError(HemErrorCode errorCode) {
            this(errorCode.getMessage(), errorCode.getCode());
        }
    }
}
