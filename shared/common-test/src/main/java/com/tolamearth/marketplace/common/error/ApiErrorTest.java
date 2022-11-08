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

import static io.micronaut.http.HttpRequest.GET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.tolamearth.marketplace.common.error.HemExceptionHandler.RenderedError;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

public interface ApiErrorTest {

  default void assertErrorCode(URI uri, HemErrorCode expectedError) {
    assertErrorCode(GET(uri), expectedError);
  }

  default void assertErrorCode(HttpRequest request, HemErrorCode expectedError) {
    try (BlockingHttpClient blockingHttpClient = getClient().toBlocking()) {
      blockingHttpClient.exchange(request,
          Argument.of(Map.class, String.class, RenderedError.class));
      fail("Expected " + expectedError.name() + " but no error thrown");
    } catch (HttpClientResponseException e) {
      HttpResponse<Map<String, RenderedError>> response = (HttpResponse<Map<String, RenderedError>>) e.getResponse();
      Map<String, RenderedError> body = response.body();

      assertNotNull(body);
      RenderedError error = body.get("error");

      try {
        assertEquals(response.status().getCode(), expectedError.getHttpCode());
        assertTrue(error.message().startsWith(expectedError.getMessage()));
        assertEquals(error.code(), expectedError.getCode());
      } catch (AssertionError ae) {
        fail("Incorrect error thrown, expected " + expectedError.name() + " but got "
            + HemErrorCode.withCode(error.code()));
      }
    } catch (IOException e) {
      // not sure when this is thrown. Network error?
      throw new RuntimeException(e);
    }

  }

  HttpClient getClient();
}
