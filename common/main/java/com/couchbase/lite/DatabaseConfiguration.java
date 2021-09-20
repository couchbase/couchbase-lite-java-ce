//
// Copyright (c) 2020, 2017 Couchbase, Inc All rights reserved.
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
import androidx.annotation.Nullable;

import com.couchbase.lite.internal.ImmutableDatabaseConfiguration;


/**
 * Configuration for opening a database.
 */
public final class DatabaseConfiguration extends AbstractDatabaseConfiguration {

    //---------------------------------------------
    // Constructors
    //---------------------------------------------

    public DatabaseConfiguration() { this((DatabaseConfiguration) null); }

    public DatabaseConfiguration(@Nullable DatabaseConfiguration config) { super(config); }

    DatabaseConfiguration(@Nullable ImmutableDatabaseConfiguration config) { super(config); }

    // for Kotlin
    DatabaseConfiguration(@Nullable String dbDirectory) { super(dbDirectory); }

    //---------------------------------------------
    // protected methods
    //---------------------------------------------
    @NonNull
    @Override
    protected DatabaseConfiguration getDatabaseConfiguration() { return this; }
}
