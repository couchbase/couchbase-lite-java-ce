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
package com.couchbase.lite

import com.couchbase.lite.internal.getCollectionConfigs
import java.security.cert.X509Certificate


/**
 * Configuration factory for new DatabaseConfigurations
 *
 * Usage:
 *      val dbConfig = DatabaseConfigurationFactory.create(...)
 */
val DatabaseConfigurationFactory: DatabaseConfiguration? = null

/**
 * Create a DatabaseConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param databasePath The directory in which the database is stored.
 *
 * @see com.couchbase.lite.DatabaseConfiguration
 */
fun DatabaseConfiguration?.create(databasePath: String? = null) = DatabaseConfiguration(databasePath ?: this?.directory)

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
 * @param database legacy parameter: use collections instead.
 * @param target (required) The max size of the log file in bytes.
 * @param collections a map of collections to their configurations
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
 * @param enableAutoPurge auto-purge enabled: defaults true..
 *
 * @see com.couchbase.lite.ReplicatorConfiguration
 */
fun ReplicatorConfiguration?.create(
    database: Database? = null,
    target: Endpoint? = null,
    collections: Map<Collection, CollectionConfiguration>? = null,
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
): ReplicatorConfiguration {
    var colls = collections ?: getCollectionConfigs(this)

    var db = database ?: this?.database
    if (db == null) {
        // no database specified: Just verify that all the collections belong to the same db.
        db = AbstractDatabase.getDbForCollections(colls?.keys)
    } else {
        // database and collections specified: verify that the collections belong to the db
        if (!colls.isNullOrEmpty()) {
            db.verifyCollections(colls.keys)
        } else {
            // database specified but no collections: configure the default collection
            val defaultCollection =
                db.defaultCollection ?: throw IllegalArgumentException(
                    "No collections provided and database has no default collection")
            colls = mapOf(defaultCollection to CollectionConfiguration())
        }
    }

    val replConfig = ReplicatorConfiguration(
        colls?.toMutableMap(),
        target ?: this?.target ?: error("Must specify a target"),
        type ?: this?.type ?: ReplicatorType.PUSH_AND_PULL,
        continuous ?: this?.isContinuous ?: false,
        authenticator ?: this?.authenticator,
        headers ?: this?.headers,
        pinnedServerCertificate ?: this?.pinnedServerX509Certificate,
        maxAttempts ?: this?.maxAttempts ?: 0,
        maxAttemptWaitTime ?: this?.maxAttemptWaitTime ?: 0,
        heartbeat ?: this?.heartbeat ?: 0,
        enableAutoPurge ?: this?.isAutoPurgeEnabled ?: true,
        db
    )

    // if there are legacy specifications, try to use them
    channels ?: this?.channels?.let { replConfig.channels = it }
    documentIDs ?: this?.documentIDs?.let { replConfig.documentIDs = it }
    pushFilter ?: this?.pushFilter?.let { replConfig.pushFilter = it }
    pullFilter ?: this?.pullFilter?.let { replConfig.pullFilter = it }
    conflictResolver ?: this?.conflictResolver?.let { replConfig.conflictResolver = it }

    return replConfig
}
