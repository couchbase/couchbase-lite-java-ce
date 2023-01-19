//
// Copyright (c) 2023 Couchbase, Inc All rights reserved.
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

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkRequest
import com.couchbase.lite.internal.AbstractWorkManagerReplicatorConfiguration
import com.couchbase.lite.internal.ReplicatorWorker
import com.couchbase.lite.internal.getCollectionConfigs
import com.couchbase.lite.internal.support.Log
import java.time.Duration
import java.util.concurrent.TimeUnit


/**
 * To create a WorkManager Replicator, subclass this interface.
 * The subclass must:
 *  + have a no-args constructor
 *  + not depend on any application state it does not construct
 *
 * The second of these two constraints is pretty serious hindrance.
 * Nearly every use of this class will be a distinct instance: the
 * values set in one will not be visible from any other.  Beyond that,
 * a given use may be part of a completely different instance of the
 * application than the any previous use:  your application may have
 * been stopped and restarted again, just to run the replication.
 * You cannot even depend on static/class/companion variables!
 *
 * The main job of your subclass is to create a `WorkManagerReplicatorConfiguration`
 * and a unique tag that will identify the replicator it starts. The tag is a
 * string that identifies the replication process: many instances of replicators,
 * possibly across many instance of the application.
 * To start a replication worker, ask your factory subclass for the appropriate
 * work manager request (one-shot, or periodic) and enqueue the request as you would
 * any other.
 * Cancel a replication process using its tag:
 * ```
 *     WorkManager.getInstance(context).cancelAllWorkByTag(factory.tag)
 * ```
 * This system depends on there being exactly one process, the replicator
 * process, with the given tag.
 */
interface WorkManagerReplicatorFactory {
    val tag: String

    fun getConfig(): WorkManagerReplicatorConfiguration?

    fun oneTimeWorkRequestBuilder(): OneTimeWorkRequest.Builder {
        val req = OneTimeWorkRequestBuilder<ReplicatorWorker>()
        addConfigFactory(req)
        return req
    }

    fun periodicWorkRequestBuilder(
        repeatInterval: Duration,
        flexInterval: Duration = repeatInterval
    ): PeriodicWorkRequest.Builder {
        val req = PeriodicWorkRequestBuilder<ReplicatorWorker>(repeatInterval, flexInterval)
        addConfigFactory(req)
        return req
    }

    fun periodicWorkRequestBuilder(
        repeatInterval: Long,
        repeatIntervalTimeUnit: TimeUnit,
        flexInterval: Long = repeatInterval,
        flexIntervalTimeUnit: TimeUnit = repeatIntervalTimeUnit
    ): PeriodicWorkRequest.Builder {
        val req = PeriodicWorkRequestBuilder<ReplicatorWorker>(
            repeatInterval,
            repeatIntervalTimeUnit,
            flexInterval,
            flexIntervalTimeUnit
        )
        addConfigFactory(req)
        return req
    }

    private fun addConfigFactory(req: WorkRequest.Builder<*, *>) {
        req.setInputData(
            Data.Builder()
                .putString(ReplicatorWorker.KEY_REPLICATOR, this::class.java.canonicalName)
                .build()
        )
    }
}

/**
 * Configuration for a `ReplicatorWorker`.
 * A subclass of `WorkManagerReplicatorFactory` produces one of these
 * that describes the `Replicator` that will be run by the `WorkManager`.
 *
 * This class hides the following properties of a com.couchbase.lite.ReplicatorConfiguration:
 * + continuous - Use `OneTimeWorkRequestBuilder` or `PeriodicWorkRequestBuilder`
 * + maxAttempts - Use `WorkRequest.Builder.setBackoffCriteria()`
 * + maxAttemptWaitTime - Use `WorkRequest.Builder.setBackoffCriteria()`
 * + heartbeat - N/A: no connection will be kept alive
 */
class WorkManagerReplicatorConfiguration private constructor(replConfig: ReplicatorConfiguration) :
    AbstractWorkManagerReplicatorConfiguration(replConfig) {

    companion object {
        /**
         * Factory method for WorkManagerReplicatorConfiguration.
         */
        fun from(target: Endpoint) = WorkManagerReplicatorConfiguration(ReplicatorConfiguration(target))

        /**
         * Factory method for WorkManagerReplicatorConfiguration.
         */
        fun from(rConfig: ReplicatorConfiguration): WorkManagerReplicatorConfiguration {
            val wConfig = WorkManagerReplicatorConfiguration(
                ReplicatorConfiguration(
                    rConfig.target,
                    getCollectionConfigs(rConfig)
                )
            )
            wConfig.type = rConfig.type
            wConfig.authenticator = rConfig.authenticator
            wConfig.headers = rConfig.headers
            wConfig.pinnedServerCertificate = rConfig.pinnedServerX509Certificate
            wConfig.enableAutoPurge = rConfig.isAutoPurgeEnabled

            if (rConfig.isContinuous != Defaults.Replicator.CONTINUOUS) {
                Log.d(LogDomain.REPLICATOR, "ReplicatorConfiguration.continuous ignored: ${rConfig.isContinuous}")
            }

            if (rConfig.heartbeat != Defaults.Replicator.HEARTBEAT) {
                Log.d(LogDomain.REPLICATOR, "ReplicatorConfiguration.heartbeat ignored: ${rConfig.heartbeat}")
            }

            if (rConfig.maxAttempts != Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT) {
                Log.d(LogDomain.REPLICATOR, "ReplicatorConfiguration.maxAttempts ignored: ${rConfig.maxAttempts}")
            }

            if (rConfig.maxAttemptWaitTime != Defaults.Replicator.MAX_ATTEMPT_WAIT_TIME) {
                Log.d(
                    LogDomain.REPLICATOR,
                    "ReplicatorConfiguration.maxAttemptWaitTime ignored: ${rConfig.maxAttemptWaitTime}"
                )
            }

            return wConfig
        }
    }
}

/**
 * Convert a `Data` object found in `WorkflowInfo`, to a `ReplicatorStatus`
 *
 * Once a `ReplicatorWorker` has been scheduled, its progress can be tracked
 * using the `LiveData` object provide by the `WorkManager`, like this:
 * ```
 * WorkManager.getInstance(context).getWorkInfosByTagLiveData(InventoryReplicatorFactory().tag)
 * ```
 *
 * That bit of code will produces a `LiveData<List<WorkflowInfo>>`.  Again, presuming that
 * client code uses the tag from the `WorkManagerReplicatorFactory`, for this and no other
 * `WorkManager` jobs, this code can be used to recover the `ReplicatorStatus` from the `Data` object
 * in the `WorkflowInfo`:
 * ```
 *     .map {
 *        if (it.isEmpty()) {
 *            null
 *        } else {
 *            it[0].progress.toReplicatorStatus()
 *        }
 *    }
 * ```
 */
fun Data.toReplicatorStatus(replId: String): ReplicatorStatus? =
    if (replId != this.getString(ReplicatorWorker.KEY_REPLICATOR)) {
        null
    } else {
        ReplicatorStatus(
            ReplicatorWorker.activityLevelFromString[this.getString(ReplicatorWorker.KEY_REPLICATION_ACTIVITY_LEVEL)]!!,
            ReplicatorProgress(
                this.getLong(ReplicatorWorker.KEY_REPLICATION_COMPLETED, 0L),
                this.getLong(ReplicatorWorker.KEY_REPLICATION_TOTAL, 0L)
            ),
            this.getString(ReplicatorWorker.KEY_REPLICATION_ERROR_MESSAGE)?.let {
                CouchbaseLiteException(
                    it,
                    CBLError.Domain.CBLITE,
                    this.getInt(ReplicatorWorker.KEY_REPLICATION_ERROR_CODE, 0)
                )
            }
        )
    }

