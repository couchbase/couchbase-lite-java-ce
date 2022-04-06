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
import androidx.annotation.VisibleForTesting;

import com.couchbase.lite.internal.CouchbaseLiteInternal;
import com.couchbase.lite.internal.core.C4Replicator;
import com.couchbase.lite.internal.replicator.AndroidConnectivityObserver;
import com.couchbase.lite.internal.replicator.NetworkConnectivityManager;


public final class Replicator extends AbstractReplicator {
    @Nullable
    private final AndroidConnectivityObserver connectivityObserver;

    /**
     * Initializes a replicator with the given configuration.
     *
     * @param config replicator configuration
     */
    public Replicator(@NonNull ReplicatorConfiguration config) {
        this(CouchbaseLiteInternal.getNetworkConnectivityManager(), config);
    }

    @VisibleForTesting
    Replicator(@Nullable NetworkConnectivityManager mgr, @NonNull ReplicatorConfiguration config) {
        super(config);

        // The replicator holds the only hard reference to the observer. The connectivity manager holds
        // only a soft ref to it.
        // Passing the ref to the getter method allows the connectivity observer to get the c4Replicator
        // even though the getter is not visible to it.
        // Note that the c4Replicator for this Replicator hasn't been created yet so we can't pass it directly
        connectivityObserver = (!config.isContinuous() || (mgr == null))
            ? null
            : new AndroidConnectivityObserver(mgr, Replicator.this::getC4Replicator);
    }

    @Override
    @NonNull
    protected C4Replicator createReplicatorForTarget(@NonNull Endpoint target) throws LiteCoreException {
        if (target instanceof URLEndpoint) { return getRemoteC4Replicator(((URLEndpoint) target).getURL()); }
        throw new IllegalStateException("unrecognized endpoint type: " + target);
    }

    @Override
    protected void handleOffline(@NonNull ReplicatorActivityLevel prevState, boolean nowOnline) {
        if (connectivityObserver != null) { connectivityObserver.handleOffline(prevState, nowOnline); }
    }
}
