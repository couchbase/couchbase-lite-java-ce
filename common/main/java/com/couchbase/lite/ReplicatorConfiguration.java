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

import java.util.Map;

import com.couchbase.lite.internal.ImmutableReplicatorConfiguration;
import com.couchbase.lite.internal.utils.Preconditions;


public final class ReplicatorConfiguration extends AbstractReplicatorConfiguration {

    //---------------------------------------------
    // Constructors
    //---------------------------------------------

    /**
     * Create a Replicator Configuration for the given database and target endpoint.
     *
     * <p>When using this constructor, the default collection of the provided
     * database will be automatically included in the configuration.</p>
     *
     * <p>If you do not intend to replicate the default collection, use
     * ReplicatorConfiguration(Endpoint) instead, and explicitly add
     * the intended collections to avoid unintended behavior.</p>
     *
     * @param database the database to be synchronized
     * @param target   the endpoint with which to synchronize it
     * @deprecated Use ReplicatorConfiguration(java.util.Collection, Endpoint)
     */
    @Deprecated
    public ReplicatorConfiguration(@NonNull Database database, @NonNull Endpoint target) {
        super(
            Preconditions.assertNotNull(database, "database"),
            configureDefaultCollection(database),
            target);
    }

    /**
     * Create a Replicator Configuration for the given target endpoint
     *
     * <p>This constructor does not configure any collections by default.
     *  Use {@link #addCollection(Collection, CollectionConfiguration)} or
     *  {@link #addCollections(java.util.Collection, CollectionConfiguration)} to
     *  configure collections to replicate.</p>
     *
     * @param target the target endpoint
     * @deprecated Use ReplicatorConfiguration(java.util.Collection, Endpoint)
     */
    @Deprecated
    public ReplicatorConfiguration(@NonNull Endpoint target) { super(null, null, target); }

    /**
     * Creates a Replicator Configuration with a set of collection configurations and
     * the target endpoint.
     *
     * @param collections the collections with configurations to replicate
     * @param target the target endpoint
     */
    public ReplicatorConfiguration(@NonNull java.util.Collection<CollectionConfiguration> collections,
                                   @NonNull Endpoint target) {
        super(null, createCollectionConfigMap(collections), target);
    }

    /**
     * Create a Replicator Configuration
     *
     * @param config the config to copy
     */
    public ReplicatorConfiguration(@NonNull ReplicatorConfiguration config) { super(config); }

    ReplicatorConfiguration(@NonNull ImmutableReplicatorConfiguration config) { super(config); }

    // For Kotlin
    ReplicatorConfiguration(@NonNull Endpoint target, @Nullable Map<Collection, CollectionConfiguration> collections) {
        super(
            (collections == null) ? null : AbstractDatabase.getDbForCollections(collections.keySet()),
            collections,
            target);
    }

    @NonNull
    @Override
    protected ReplicatorConfiguration getReplicatorConfiguration() { return this; }
}
