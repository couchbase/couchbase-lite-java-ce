//
// AbstractReplicator.java
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

import android.support.annotation.NonNull;

import com.couchbase.lite.internal.core.C4Replicator;


public final class Replicator extends AbstractReplicator {
    /**
     * Initializes a replicator with the given configuration.
     *
     * @param config The Replicator configuration object
     */
    public Replicator(@NonNull ReplicatorConfiguration config) { super(config); }

    @Override
    protected C4Replicator createReplicatorForTarget(Endpoint target) throws LiteCoreException {
        if (target instanceof URLEndpoint) { return getRemoteC4Replicator(((URLEndpoint) target).getURL()); }
        throw new IllegalStateException("unrecognized endpoint type: " + target);
    }
}
