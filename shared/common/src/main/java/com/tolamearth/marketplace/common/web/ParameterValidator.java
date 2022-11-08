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

package com.tolamearth.marketplace.common.web;

import com.tolamearth.marketplace.common.error.HemErrorCode;
import com.tolamearth.marketplace.common.error.HemException;
import java.util.function.Consumer;

public interface ParameterValidator {

  default <P> void validateParameter(P parameter, Consumer<P> validator) {
    try {
      validator.accept(parameter);
    } catch (NullPointerException exception) {
      throw new HemException(HemErrorCode.MISSING_REQUIRED_FIELD, exception);
    } catch (IllegalArgumentException exception) {
      throw new HemException(HemErrorCode.INVALID_DATA, exception);
    }
  }

}
