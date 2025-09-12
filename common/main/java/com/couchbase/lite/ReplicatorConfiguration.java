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

import java.util.Map;

import com.couchbase.lite.internal.ImmutableReplicatorConfiguration;


public final class ReplicatorConfiguration extends AbstractReplicatorConfiguration {

    //---------------------------------------------
    // Constructors
    //---------------------------------------------

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
    ReplicatorConfiguration(@NonNull Endpoint target, @NonNull Map<Collection, CollectionConfiguration> collections) {
        super(
                AbstractDatabase.getDbForCollections(collections.keySet()),
                collections,
                target);
    }

    @NonNull
    @Override
    protected ReplicatorConfiguration getReplicatorConfiguration() { return this; }
}
