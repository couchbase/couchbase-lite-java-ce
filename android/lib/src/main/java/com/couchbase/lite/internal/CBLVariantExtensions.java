//
// Copyright (c) 2020 Couchbase, Inc.
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
package com.couchbase.lite.internal;

import android.content.Context;

import androidx.annotation.NonNull;

import com.couchbase.lite.CouchbaseLiteException;


public final class CBLVariantExtensions {
    private CBLVariantExtensions() { }

    public static void initVariant(@NonNull Object lock, @NonNull Context ctxt) throws CouchbaseLiteException {
        throw new CouchbaseLiteException("Vector Search not supported on this platform");
    }
}
