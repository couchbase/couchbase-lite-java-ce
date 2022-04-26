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

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.security.cert.Certificate;
import java.util.List;

import com.couchbase.lite.Endpoint;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;
import com.couchbase.lite.internal.core.C4Socket;
import com.couchbase.lite.internal.replicator.AbstractCBLWebSocket;
import com.couchbase.lite.internal.replicator.CBLCookieStore;
import com.couchbase.lite.internal.utils.Fn;


public class SocketFactory {
    @NonNull
    private final Endpoint endpoint;
    @NonNull
    private final CBLCookieStore cookieStore;
    @NonNull
    private final Fn.Consumer<List<Certificate>> serverCertsListener;

    @Nullable
    private Fn.Consumer<C4Socket> listener;

    public SocketFactory(
        @NonNull ReplicatorConfiguration config,
        @NonNull CBLCookieStore cookieStore,
        @NonNull Fn.Consumer<List<Certificate>> serverCertsListener) {
        this.endpoint = config.getTarget();
        this.cookieStore = cookieStore;
        this.serverCertsListener = serverCertsListener;
    }

    public C4Socket createSocket(long handle, String scheme, String hostname, int port, String path, byte[] options) {
        C4Socket socket = null;

        if (endpoint instanceof URLEndpoint) {
            if (Build.VERSION.SDK_INT < 21) {
                throw new UnsupportedOperationException("Couchbase sockets require Android version >= 21");
            }

            socket = AbstractCBLWebSocket.createCBLWebSocket(
                handle, scheme, hostname, port, path, options, cookieStore, serverCertsListener);
        }

        if (listener != null) { listener.accept(socket); }

        if (socket != null) { return socket; }

        throw new UnsupportedOperationException("Unrecognized endpoint type: " + endpoint.getClass());
    }

    @VisibleForTesting
    public void setListener(@Nullable Fn.Consumer<C4Socket> listener) { this.listener = listener; }
}