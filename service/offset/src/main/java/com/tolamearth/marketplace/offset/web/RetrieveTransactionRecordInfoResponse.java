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

package com.tolamearth.marketplace.offset.web;

import com.tolamearth.marketplace.offset.ListingTransactionState;

import java.util.Map;

record RetrieveTransactionRecordInfoResponse(Map<String, String> request, ListingTransactionState state){
}
/*
{
    "request" : {
        "txn_id": ,  // The transaction id of the transaction
        "txn_type":  // The type of the transaction
    }
    "state" : ,     // The listing or purchasing state. See link below
}
 */