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

package com.tolamearth.marketplace.common;

import com.tolamearth.marketplace.common.error.HemErrorCode;
import com.tolamearth.marketplace.common.error.HemException;
import java.util.Objects;

public class HederaAccount { // TODO: convert to use or replace with hedera sdk

    private int shardId;
    private int realmId;
    private int accountNumber;

    public HederaAccount() {
    }

    public HederaAccount(String id) {
        parseId(id);
    }

    public String getId() {
        return shardId + "." + realmId + "." + accountNumber;
    }

    public void setId(String id) {
        parseId(id);
    }

    private void parseId(String id) {
        var parts = id.split("\\.");
        if (parts.length != 3) throw new HemException(HemErrorCode.INVALID_DATA);
        try {
            shardId = Integer.parseInt(parts[0]);
            realmId = Integer.parseInt(parts[1]);
            accountNumber = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new HemException(HemErrorCode.INVALID_DATA, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HederaAccount that = (HederaAccount) o;
        return shardId == that.shardId && realmId == that.realmId && accountNumber == that.accountNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shardId, realmId, accountNumber);
    }
}
