//
// Copyright (c) 2017 Couchbase, Inc All rights reserved.
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

import java.lang.ref.WeakReference;

import com.couchbase.lite.internal.CouchbaseLiteInternal;
import com.couchbase.lite.internal.core.C4Replicator;
import com.couchbase.lite.internal.replicator.AndroidConnectivityObserver;
import com.couchbase.lite.internal.replicator.NetworkConnectivityManager;


public final class Replicator extends AbstractReplicator {
    private final AndroidConnectivityObserver connectivityObserver;

    /**
     * Initializes a replicator with the given configuration.
     *
     * @param config the configuration
     */
    public Replicator(ReplicatorConfiguration config) {
        super(config);

        // This little bit of hackery is necessary sot that:
        // 1) The gc can collect this Replicator, even if it is registered for connectivity events
        // 2) the connectivity observer can get the c4Replicator, even though the getter is not visible to it.
        //    Note that the c4Replicator for this Replicator hasn't been created yet.
        final WeakReference<Replicator> weakThis = new WeakReference<>(this);
        final NetworkConnectivityManager mgr = CouchbaseLiteInternal.getNetworkConnectivityManager();
        connectivityObserver = (!config.isContinuous() || (mgr == null))
            ? null
            : new AndroidConnectivityObserver(
                mgr,
                () -> {
                    final Replicator repl = weakThis.get();
                    if (repl == null) { return null; }
                    return repl.getC4Replicator();
                });
    }


    @Override
    protected C4Replicator createReplicatorForTarget(Endpoint target) throws LiteCoreException {
        if (target instanceof URLEndpoint) { return getRemoteC4Replicator(((URLEndpoint) target).getURL()); }
        throw new IllegalStateException("unrecognized endpoint type: " + target);
    }

    @Override
    protected void handleOffline(ActivityLevel prevState, boolean nowOnline) {
        if (connectivityObserver != null) { connectivityObserver.handleOffline(prevState, nowOnline); }
    }
}
