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
fun DatabaseConfiguration?.newConfig(databasePath: String? = null): DatabaseConfiguration {
    val config = DatabaseConfiguration()

    (databasePath ?: this?.directory)?.let { config.directory = it }

    return config
}

/**
 * Create a ReplicatorConfiguration, overriding the receiver's
 * values with the passed parameters.
 *
 * Note: A document that is blocked by a document Id filter will not be auto-purged
 *       regardless of the setting of the enableAutoPurge property
 *
 * @param target (required) The replication endpoint.
 * @param collections a map of collections to be replicated, to their configurations.
 * @param type replicator type: push, pull, or push and pull: default is push and pull.
 * @param continuous continuous flag: true for continuous, false by default.
 * @param authenticator connection authenticator.
 * @param headers extra HTTP headers to send in all requests to the remote target.
 * @param pinnedServerCertificate target server's SSL certificate.
 * @param maxAttempts max retry attempts after connection failure.
 * @param maxAttemptWaitTime max time between retry attempts (exponential backoff).
 * @param heartbeat heartbeat interval, in seconds.
 * @param enableAutoPurge auto-purge enabled.
 * @param acceptParentDomainCookies Advanced: accept cookies for parent domains.
 *
 * @see com.couchbase.lite.ReplicatorConfiguration
 */
fun ReplicatorConfiguration?.newConfig(
    target: Endpoint? = null,
    collections: Map<Collection, CollectionConfiguration>? = null,
    type: ReplicatorType? = null,
    continuous: Boolean? = null,
    authenticator: Authenticator? = null,
    headers: Map<String, String>? = null,
    pinnedServerCertificate: X509Certificate? = null,
    maxAttempts: Int? = null,
    maxAttemptWaitTime: Int? = null,
    heartbeat: Int? = null,
    enableAutoPurge: Boolean? = null,
    acceptParentDomainCookies: Boolean? = null
): ReplicatorConfiguration {
    val config = ReplicatorConfiguration(
        target ?: this?.target ?: throw IllegalArgumentException("A ReplicatorConfiguration must specify an endpoint"),
        collections ?: getCollectionConfigs(this)
    )

    copyReplConfig(
        this,
        config,
        type,
        continuous,
        authenticator,
        headers,
        maxAttempts,
        maxAttemptWaitTime,
        heartbeat,
        enableAutoPurge,
        acceptParentDomainCookies
    )

    (pinnedServerCertificate ?: this?.pinnedServerX509Certificate)?.let { config.setPinnedServerX509Certificate(it) }

    return config
}

/**
 * Create a DatabaseConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param databasePath The directory in which the database is stored.
 *
 * @see com.couchbase.lite.DatabaseConfiguration
 * @deprecated Use ReplicatorConfigurationFactory().newConfig(String?, EncryptionKey?)
 */
@Deprecated(
    "Use DatabaseConfigurationFactory.newConfig(String?, EncryptionKey?)",
    replaceWith = ReplaceWith("DatabaseConfigurationFactory.newConfig(String?, EncryptionKey?)")
)
fun DatabaseConfiguration?.create(databasePath: String? = null) = this.newConfig(databasePath)

/**
 * Configuration factory for new ReplicatorConfigurations
 *
 * Usage:
 *      val replConfig = ReplicatorConfigurationFactory.create(...)
 */
val ReplicatorConfigurationFactory: ReplicatorConfiguration? = null

/**
 * Create a ReplicatorConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * Note: A document that is blocked by a document Id filter will not be auto-purged
 *       regardless of the setting of the enableAutoPurge property
 *
 * Warning: This factory method configures only the default collection!
 *          Using it on a configuration that describes any collections other than the default
 *          will loose all information associated with those collections
 *
 * @param database the local database
 * @param target (required) The replication endpoint.
 * @param type replicator type: push, pull, or push and pull: default is push and pull.
 * @param continuous continuous flag: true for continuous. False by default.
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
 * @param enableAutoPurge auto-purge enabled..
 * @param acceptParentDomainCookies Advanced: accept cookies for parent domains.
 *
 * @see com.couchbase.lite.ReplicatorConfiguration
 * @deprecated Use ReplicatorConfigurationFactory().create(Endpoint?, Map<Collection, CollectionConfiguration>, ...)
 */
@Suppress("DEPRECATION")
@Deprecated(
    "Use ReplicatorConfigurationFactory.create(Endpoint?, Map<Collection, CollectionConfiguration>, ...)",
    replaceWith = ReplaceWith("ReplicatorConfigurationFactory.create(Endpoint?, Map<Collection, CollectionConfiguration>, ...)")
)
fun ReplicatorConfiguration?.create(
    database: Database? = null,
    target: Endpoint? = null,
    type: ReplicatorType? = null,
    continuous: Boolean? = null,
    authenticator: Authenticator? = null,
    headers: Map<String, String>? = null,
    pinnedServerCertificate: ByteArray? = null,
    channels: List<String>? = null,
    documentIDs: List<String>? = null,
    pushFilter: ReplicationFilter? = null,
    pullFilter: ReplicationFilter? = null,
    conflictResolver: ConflictResolver? = null,
    maxAttempts: Int? = null,
    maxAttemptWaitTime: Int? = null,
    heartbeat: Int? = null,
    enableAutoPurge: Boolean? = null,
    acceptParentDomainCookies: Boolean? = null
): ReplicatorConfiguration {
    // ReplicatorConfiguration.getDatabase throws an ISE on null database
    val db = database ?: try {
        this?.database
    } catch (e: IllegalStateException) {
        null
    }
    ?: throw IllegalArgumentException("A ReplicatorConfiguration must specify a database")
    checkDbCollections(db, this?.collections)

    val config = ReplicatorConfiguration(
        db,
        target ?: this?.target ?: throw IllegalArgumentException("A ReplicatorConfiguration must specify an endpoint")
    )

    copyReplConfig(
        this,
        config,
        type,
        continuous,
        authenticator,
        headers,
        maxAttempts,
        maxAttemptWaitTime,
        heartbeat,
        enableAutoPurge,
        acceptParentDomainCookies
    )

    copyLegacyReplConfig(
        this,
        config,
        pinnedServerCertificate,
        channels,
        documentIDs,
        pushFilter,
        pullFilter,
        conflictResolver
    )

    return config
}
