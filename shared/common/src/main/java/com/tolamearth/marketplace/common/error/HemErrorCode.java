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

import java.util.Arrays;

public enum HemErrorCode {
    MISSING_REQUIRED_FIELD(1001, 400, "Missing required field"),
    INVALID_DATA(1003, 400, "Invalid data"),
    INVALID_DATA_FORMAT(1002, 415, "Invalid data format"),
    UNKNOWN_RESOURCE(1004, 404, "Unknown resource"),
    ALREADY_IN_PROGRESS(1005, 400, "Already in progress");

    private final Integer code;
    private final Integer httpCode;
    private final String message;

    HemErrorCode(int code, int httpCode, String message) {
        this.code = code;
        this.httpCode = httpCode;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Integer getHttpCode() {
        return httpCode;
    }

    static HemErrorCode withCode(Integer code){
        return Arrays.stream(HemErrorCode.values()).filter(errorCode -> errorCode.getCode().equals(code)).findFirst().orElse(null);
    }
}
