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
package com.couchbase.lite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.security.cert.X509Certificate;
import java.util.Map;

import com.couchbase.lite.internal.ImmutableReplicatorConfiguration;
import com.couchbase.lite.internal.utils.Preconditions;


public final class ReplicatorConfiguration extends AbstractReplicatorConfiguration {

    //---------------------------------------------
    // Constructors
    //---------------------------------------------

    /**
     * Create a Replicator Configuration
     *
     * @param database the database to be synchronized
     * @param target   the endpoint with which to synchronize it
     * @deprecated Use ReplicatorConfiguration.addCollection()
     */
    @Deprecated
    public ReplicatorConfiguration(@NonNull Database database, @NonNull Endpoint target) {
        super(
            Preconditions.assertNotNull(database, "database"),
            configureDefaultCollection(database),
            target);
    }

    /**
     * Create a Replicator Configuration
     *
     * @param target the target endpoint
     */
    public ReplicatorConfiguration(@NonNull Endpoint target) { super(null, null, target); }

    /**
     * Create a Replicator Configuration
     *
     * @param config the config to copy
     */
    public ReplicatorConfiguration(@NonNull ReplicatorConfiguration config) { super(config); }

    ReplicatorConfiguration(@NonNull ImmutableReplicatorConfiguration config) { super(config); }

    //    // for Kotlin
    @SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.UnnecessaryFullyQualifiedName"})
    ReplicatorConfiguration(
        @Nullable Map<Collection, CollectionConfiguration> collections,
        @NonNull Endpoint target,
        @NonNull com.couchbase.lite.ReplicatorType type,
        boolean continuous,
        @Nullable Authenticator authenticator,
        @Nullable Map<String, String> headers,
        @Nullable X509Certificate pinnedServerCertificate,
        int maxAttempts,
        int maxAttemptWaitTime,
        int heartbeat,
        boolean enableAutoPurge,
        @Nullable Database database) {
        super(
            collections,
            Preconditions.assertNotNull(target, "target"),
            Preconditions.assertNotNull(type, "type"),
            continuous,
            authenticator,
            headers,
            pinnedServerCertificate,
            maxAttempts,
            maxAttemptWaitTime,
            verifyHeartbeat(heartbeat),
            enableAutoPurge,
            database);
    }

    @NonNull
    @Override
    protected ReplicatorConfiguration getReplicatorConfiguration() { return this; }
}
