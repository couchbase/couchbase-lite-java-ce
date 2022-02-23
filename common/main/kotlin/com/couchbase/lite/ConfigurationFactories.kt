//
// Copyright (c) 2021 Couchbase, Inc All rights reserved.
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
package com.couchbase.lite

import java.security.cert.X509Certificate


/**
 * Configuration factory for new ReplicatorConfigurations
 * Usage:
 *     val replConfig = ReplicatorConfigurationFactory.create(...)
 */
val ReplicatorConfigurationFactory: ReplicatorConfiguration? = null

/**
 * Create a FullTextIndexConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param database (required) the local database.
 * @param target (required) The max size of the log file in bytes.
 * @param type replicator type: push, pull, or push and pull: default is push and pull.
 * @param continuous continuous flag: true for continuous, false by default.
 * @param authenticator connection authenticator.
 * @param headers extra HTTP headers to send in all requests to the remote target.
 * @param pinnedServerCertificate target server's SSL certificate.
 * @param channels Sync Gateway channel names.
 * @param documentIDs IDs of documents to be replicated: default is all documents.
 * @param pushFilter filter for pushed documents.
 * @param pullFilter filter for pulled documents.
 * @param conflictResolver conflict resolver.
 * @param maxAttempts max retry attempts after connection failure.
 * @param maxAttemptWaitTime max time between retry attempts (exponential backoff).
 * @param heartbeat heartbeat interval, in seconds.
 * @param enableAutoPurge auto-purge enabled.
 *
 * @see com.couchbase.lite.ReplicatorConfiguration
 */
fun ReplicatorConfiguration?.create(
    database: Database? = null,
    target: Endpoint? = null,
    type: ReplicatorType? = null,
    continuous: Boolean? = null,
    authenticator: Authenticator? = null,
    headers: Map<String, String>? = null,
    pinnedServerCertificate: X509Certificate? = null,
    channels: List<String>? = null,
    documentIDs: List<String>? = null,
    pushFilter: ReplicationFilter? = null,
    pullFilter: ReplicationFilter? = null,
    conflictResolver: ConflictResolver? = null,
    maxAttempts: Int? = null,
    maxAttemptWaitTime: Int? = null,
    heartbeat: Int? = null,
    enableAutoPurge: Boolean? = null
) = ReplicatorConfiguration(
    database ?: this?.database ?: error("Must specify a database"),
    type ?: this?.type ?: ReplicatorType.PUSH_AND_PULL,
    continuous ?: this?.isContinuous ?: false,
    authenticator ?: this?.authenticator,
    headers ?: this?.headers,
    pinnedServerCertificate ?: this?.pinnedServerX509Certificate,
    channels ?: this?.channels,
    documentIDs ?: this?.documentIDs,
    pushFilter ?: this?.pushFilter,
    pullFilter ?: this?.pullFilter,
    conflictResolver ?: this?.conflictResolver,
    maxAttempts ?: this?.maxAttempts ?: 0,
    maxAttemptWaitTime ?: this?.maxAttemptWaitTime ?: 0,
    heartbeat ?: this?.heartbeat ?: 0,
    enableAutoPurge ?: this?.isAutoPurgeEnabled ?: false,
    target ?: this?.target ?: error("Must specify a target")
)

val DatabaseConfigurationFactory: DatabaseConfiguration? = null
fun DatabaseConfiguration?.create(databasePath: String? = null) = DatabaseConfiguration(databasePath ?: this?.directory)

