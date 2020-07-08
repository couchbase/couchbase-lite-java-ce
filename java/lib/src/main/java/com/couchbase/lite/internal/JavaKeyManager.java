//
// Copyright (c) 2020 Couchbase, Inc All rights reserved.
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

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.Date;


public class JavaKeyManager extends KeyManager {

    @Nullable
    @Override
    public KeyPair generateKeyPair(
        @NonNull String alias,
        @NonNull KeyAlgorithm algorithm,
        @NonNull KeySize keySize,
        @NonNull BigInteger serial,
        @NonNull Date expiration) {
        throw new UnsupportedOperationException("KeyManger.generateKeyPair not supported in CBL Community Edition");
    }

    @NonNull
    @Override
    public byte[] getKeyData(long keyToken) {
        throw new UnsupportedOperationException("KeyManger.getKeyData not supported in CBL Community Edition");
    }

    @NonNull
    @Override
    public byte[] decrypt(long keyToken, @NonNull byte[] data) {
        throw new UnsupportedOperationException("KeyManger.decrypt not supported in CBL Community Edition");
    }

    @NonNull
    @Override
    public byte[] signKey(long keyToken, int digestAlgorithm, @NonNull byte[] data) {
        throw new UnsupportedOperationException("KeyManger.signKey not supported in CBL Community Edition");
    }

    @Override
    public void free(long keyToken) {
        throw new UnsupportedOperationException("KeyManger.free not supported in CBL Community Edition");
    }
}
