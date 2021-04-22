//
// Copyright (c) 2020, 2019 Couchbase, Inc All rights reserved.
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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

import com.couchbase.lite.internal.ImmutableReplicatorConfiguration;


public final class ReplicatorConfiguration extends AbstractReplicatorConfiguration {

    //---------------------------------------------
    // Constructors
    //---------------------------------------------
    public ReplicatorConfiguration(@NonNull Database database, @NonNull Endpoint target) { super(database, target); }

    public ReplicatorConfiguration(@NonNull ReplicatorConfiguration config) { super(config); }

    ReplicatorConfiguration(@NonNull ImmutableReplicatorConfiguration config) { super(config); }

    // for Kotlin
    @SuppressWarnings("PMD.ExcessiveParameterList")
    ReplicatorConfiguration(
        @NonNull Database database,
        @NonNull Replicator.Type type,
        boolean continuous,
        @Nullable Authenticator authenticator,
        @Nullable Map<String, String> headers,
        @Nullable byte[] pinnedServerCertificate,
        @Nullable List<String> channels,
        @Nullable List<String> documentIDs,
        @Nullable ReplicationFilter pushFilter,
        @Nullable ReplicationFilter pullFilter,
        @Nullable ConflictResolver conflictResolver,
        int maxRetries,
        long maxRetryWaitTime,
        long heartbeat,
        @NonNull Endpoint target,
        boolean acceptOnlySelfSignedServerCertificate) {
        super(
            database,
            type,
            continuous,
            authenticator,
            headers,
            pinnedServerCertificate,
            channels,
            documentIDs,
            pushFilter,
            pullFilter,
            conflictResolver,
            maxRetries,
            maxRetryWaitTime,
            heartbeat,
            target);
    }

    @Override
    ReplicatorConfiguration getReplicatorConfiguration() { return this; }
}
