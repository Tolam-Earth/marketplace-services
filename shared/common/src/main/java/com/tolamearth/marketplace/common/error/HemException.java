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

public class HemException extends RuntimeException {

    final HemErrorCode code;

    public HemException(HemErrorCode code) {
        super("HemException with code " + code.getCode() + " encountered.");
        this.code = code;
    }

    public HemException(HemErrorCode code, Throwable throwable){

        super("HemException with code " + code.getCode() + " encountered, caused by: " + throwable.getClass().getSimpleName() + " - " +throwable.getMessage(), throwable);
        this.code = code;
    }

    public HemErrorCode getCode() {
        return code;
    }
}
