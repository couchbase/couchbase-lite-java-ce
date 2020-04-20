//
// Copyright (c) 2019 Couchbase, Inc All rights reserved.
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

import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.internal.core.C4Socket;
import com.couchbase.lite.internal.replicator.AbstractCBLWebSocket;


public class SocketFactory {
    public SocketFactory(@NonNull ReplicatorConfiguration ignore) { }

    public C4Socket createSocket(long handle, String scheme, String hostname, int port, String path, byte[] options) {
        if (Build.VERSION.SDK_INT < 21) {
            throw new UnsupportedOperationException("Couchbase sockets require Android version >= 21");
        }

        return AbstractCBLWebSocket.createCBLWebSocket(handle, scheme, hostname, port, path, options);
    }
}