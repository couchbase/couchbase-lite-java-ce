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
package com.couchbase.lite.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.security.cert.Certificate;
import java.util.List;

import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.internal.core.C4Socket;
import com.couchbase.lite.internal.replicator.CBLCookieStore;
import com.couchbase.lite.internal.utils.Fn;


public class SocketFactory extends AbstractSocketFactory {

    public SocketFactory(
        @NonNull ReplicatorConfiguration config,
        @NonNull CBLCookieStore cookieStore,
        @NonNull Fn.Consumer<List<Certificate>> serverCertsListener) {
        super(config, cookieStore, serverCertsListener);
    }

    @Nullable
    @Override
    protected C4Socket createPlatformSocket(long handle) { return null; }
}
