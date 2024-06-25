//
// Copyright (c) 2024 Couchbase, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.couchbase.lite;

import androidx.annotation.NonNull;

import com.couchbase.lite.internal.core.C4QueryIndex;


/**
 * QueryIndex object represents an existing index in the collection.
 */
public class QueryIndex {
    @NonNull
    private final Collection collection;
    @NonNull
    private final String name;

    QueryIndex(@NonNull Collection collection, @NonNull String name, @NonNull C4QueryIndex ignore) {
        this.collection = collection;
        this.name = name;
    }

    @NonNull
    public Collection getCollection() { return collection; }

    @NonNull
    public String getName() { return name; }
}
